package dev.granite.graniteskills.fabric.ability;

import dev.granite.graniteskills.api.ability.Abilities;
import dev.granite.graniteskills.api.skill.Skills;
import dev.granite.graniteskills.api.stat.StatModifier;
import dev.granite.graniteskills.api.stat.Stats;
import dev.granite.graniteskills.api.util.GraniteSkillsModifier.Operation;
import dev.granite.graniteskills.api.util.NumberUtil;
import dev.granite.graniteskills.common.message.type.AbilityMessage;
import dev.granite.graniteskills.common.scheduler.TaskRunnable;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.common.util.text.TextUtil;
import dev.granite.graniteskills.fabric.FabricBrewingOwnerTracker;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Alchemy passives. {@link Abilities#ALCHEMIST} stores a brew-time duration bonus on the potion
 * (Bukkit {@code duration_bonus} PDC) and applies it when the potion is drunk.
 */
public final class FabricAlchemyAbilities extends FabricAbilityImpl {

    private static final String WISE_EFFECT = "AbilityModifier-WiseEffect";
    private static final String PUBLIC_BUKKIT_VALUES = "PublicBukkitValues";
    private static final String DURATION_BONUS_KEY = "graniteskills:duration_bonus";

    public FabricAlchemyAbilities(FabricGraniteSkills plugin) {
        super(plugin, Abilities.ALCHEMIST, Abilities.BREWER, Abilities.SPLASHER, Abilities.LINGERING, Abilities.WISE_EFFECT);
        plugin.getScheduler().timerSync(new TaskRunnable() {
            @Override
            public void run() {
                refreshWiseEffect();
            }
        }, 1, 10, TimeUnit.SECONDS);
    }

    /**
     * After {@code BrewingStandBlockEntity#doBrew}: stamp Alchemist duration onto result bottles
     * for the stand owner (Bukkit {@code BrewEvent} + delayed inventory rewrite).
     */
    public static void onBrewComplete(ServerLevel world, BlockPos pos, NonNullList<ItemStack> slots) {
        FabricGraniteSkills plugin = FabricGraniteSkills.getInstance();
        if (plugin == null) {
            return;
        }
        FabricAlchemyAbilities self = plugin.getAbilityManager().getAbilityImpl(FabricAlchemyAbilities.class);
        var ability = Abilities.ALCHEMIST;
        if (self.isDisabled(ability)) {
            return;
        }
        UUID ownerId = FabricBrewingOwnerTracker.getOwner(world, pos);
        if (ownerId == null) {
            return;
        }
        ServerPlayer player = world.getServer().getPlayerList().getPlayer(ownerId);
        if (player == null) {
            return;
        }
        if (self.failsChecks(player, ability)) {
            return;
        }
        User user = plugin.getUserManager().getUser(ownerId);
        Locale locale = user.getLocale();
        for (int i = 0; i < 3; i++) {
            ItemStack stack = slots.get(i);
            if (stack.isEmpty()) {
                continue;
            }
            self.applyDurationData(stack, user, locale);
        }
    }

    /**
     * Drink path: extend potion effect duration by the stored Alchemist bonus.
     */
    public static void onConsume(FabricGraniteSkills plugin, ServerPlayer player, ItemStack stack) {
        if (plugin == null || player == null || stack == null || stack.isEmpty()) {
            return;
        }
        if (!stack.is(Items.POTION)) {
            return;
        }
        FabricAlchemyAbilities self = plugin.getAbilityManager().getAbilityImpl(FabricAlchemyAbilities.class);
        var ability = Abilities.ALCHEMIST;
        if (self.isDisabled(ability) || self.failsChecks(player, ability)) {
            return;
        }
        int durationBonus = readDurationBonus(stack);
        if (durationBonus <= 0) {
            return;
        }
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        if (contents == null) {
            return;
        }
        double sugarRush = self.sugarRushMultiplier(player);
        for (MobEffectInstance effect : contents.getAllEffects()) {
            Holder<MobEffect> type = effect.getEffect();
            if (type.value().isInstantaneous()) {
                continue;
            }
            int duration = effect.getDuration() + durationBonus;
            if (type.equals(MobEffects.SPEED) || type.equals(MobEffects.JUMP_BOOST)) {
                duration = (int) (duration * sugarRush);
            }
            player.addEffect(new MobEffectInstance(type, duration, effect.getAmplifier()));
        }
        for (MobEffectInstance effect : contents.customEffects()) {
            if (effect.getEffect().value().isInstantaneous()) {
                continue;
            }
            player.addEffect(
                    new MobEffectInstance(
                            effect.getEffect(),
                            effect.getDuration() + durationBonus,
                            effect.getAmplifier()));
        }
    }

    /**
     * Applies Alchemist duration metadata when checks pass. Returns the stored bonus ticks (0 if skipped).
     */
    public int applyAlchemist(ServerPlayer player, ItemStack stack) {
        var ability = Abilities.ALCHEMIST;
        if (isDisabled(ability) || failsChecks(player, ability) || stack == null || stack.isEmpty()) {
            return 0;
        }
        User user = plugin.getUserManager().getUser(player.getUUID());
        return applyDurationData(stack, user, user.getLocale());
    }

    public static int readDurationBonus(ItemStack stack) {
        CompoundTag root = customRoot(stack);
        if (root == null) {
            return 0;
        }
        CompoundTag pbv = root.getCompound(PUBLIC_BUKKIT_VALUES).orElse(null);
        if (pbv == null) {
            return pbvFallback(root);
        }
        if (pbv.contains(DURATION_BONUS_KEY)) {
            return pbv.getIntOr(DURATION_BONUS_KEY, 0);
        }
        return pbvFallback(root);
    }

    private static int pbvFallback(CompoundTag root) {
        if (root.contains(DURATION_BONUS_KEY)) {
            return root.getIntOr(DURATION_BONUS_KEY, 0);
        }
        return 0;
    }

    private int applyDurationData(ItemStack stack, User user, Locale locale) {
        var ability = Abilities.ALCHEMIST;
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        if (contents == null || !isApplicablePotion(contents)) {
            return 0;
        }
        if (readDurationBonus(stack) > 0) {
            return readDurationBonus(stack);
        }
        int originalDuration = maxDuration(contents);
        if (originalDuration <= 0) {
            return 0;
        }
        double multiplier = 1.0 + (getValue(ability, user) / 100.0);
        int duration = (int) (originalDuration * multiplier);
        int durationBonus = duration - originalDuration;
        if (durationBonus <= 0) {
            return 0;
        }
        writeDurationBonus(stack, durationBonus);
        if (Abilities.ALCHEMIST.optionBoolean("add_item_lore", true)) {
            String raw =
                    TextUtil.replace(
                            plugin.getMsg(AbilityMessage.ALCHEMIST_LORE, locale),
                            "{duration}",
                            formatDuration(durationBonus),
                            "{value}",
                            NumberUtil.format1((multiplier - 1.0) * 100.0));
            String line = plugin.getMessageProvider().applyFormatting(raw);
            appendLoreLine(stack, Component.literal(line));
        }
        return durationBonus;
    }

    private static boolean isApplicablePotion(PotionContents contents) {
        Optional<Holder<Potion>> potion = contents.potion();
        if (potion.isEmpty()) {
            return maxDuration(contents) > 0;
        }
        Holder<Potion> holder = potion.get();
        if (holder.equals(Potions.WATER)
                || holder.equals(Potions.AWKWARD)
                || holder.equals(Potions.MUNDANE)
                || holder.equals(Potions.THICK)
                || holder.equals(Potions.HEALING)
                || holder.equals(Potions.STRONG_HEALING)
                || holder.equals(Potions.HARMING)
                || holder.equals(Potions.STRONG_HARMING)) {
            return false;
        }
        return !holder.value().hasInstantEffects() && maxDuration(contents) > 0;
    }

    private static int maxDuration(PotionContents contents) {
        int max = 0;
        for (MobEffectInstance effect : contents.getAllEffects()) {
            max = Math.max(max, effect.getDuration());
        }
        return max;
    }

    private static void writeDurationBonus(ItemStack stack, int durationBonus) {
        CompoundTag root = customRoot(stack);
        if (root == null) {
            root = new CompoundTag();
        } else {
            root = root.copy();
        }
        CompoundTag pbv = root.getCompound(PUBLIC_BUKKIT_VALUES).orElseGet(CompoundTag::new);
        pbv.putInt(DURATION_BONUS_KEY, durationBonus);
        root.put(PUBLIC_BUKKIT_VALUES, pbv);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
    }

    private static CompoundTag customRoot(ItemStack stack) {
        CustomData comp = stack.get(DataComponents.CUSTOM_DATA);
        return comp == null ? null : comp.copyTag();
    }

    private static void appendLoreLine(ItemStack stack, Component line) {
        ItemLore existing = stack.get(DataComponents.LORE);
        List<Component> lines = new ArrayList<>();
        if (existing != null) {
            lines.addAll(existing.lines());
        }
        lines.add(line);
        stack.set(DataComponents.LORE, new ItemLore(lines));
    }

    static String formatDuration(int durationTicks) {
        int seconds = durationTicks / 20;
        int minutes = seconds / 60;
        return minutes + ":" + String.format(Locale.ROOT, "%02d", seconds % 60);
    }

    private double sugarRushMultiplier(ServerPlayer player) {
        var ability = Abilities.SUGAR_RUSH;
        if (isDisabled(ability) || failsChecks(player, ability)) {
            return 1.0;
        }
        User user = plugin.getUserManager().getUser(player.getUUID());
        return 1.0 + getValue(ability, user) / 100.0;
    }

    /**
     * Bukkit Splasher: {@code 1 + (value/100 * affectedPlayers)} when unlocked; otherwise {@code 1.0}.
     */
    public static double getSplasherMultiplier(ServerPlayer shooter, int affectedPlayerCount) {
        FabricGraniteSkills plugin = FabricGraniteSkills.getInstance();
        if (plugin == null || shooter == null || affectedPlayerCount <= 0) {
            return 1.0;
        }
        FabricAlchemyAbilities self = plugin.getAbilityManager().getAbilityImpl(FabricAlchemyAbilities.class);
        var ability = Abilities.SPLASHER;
        if (self.isDisabled(ability) || self.failsChecks(shooter, ability)) {
            return 1.0;
        }
        User user = plugin.getUserManager().getUser(shooter.getUUID());
        return 1.0 + (self.getValue(ability, user) / 100.0 * affectedPlayerCount);
    }

    /**
     * Bukkit {@code LingeringPotionSplashEvent}: multiply cloud {@code radiusPerTick} by
     * {@code 1 - value/100} and {@code radiusOnUse} by {@code 1 - secondary/100} (floor 0.01).
     *
     * @return true when decay multipliers were applied
     */
    public static boolean applyLingering(ServerPlayer player, AreaEffectCloud cloud) {
        return applyLingering(player, cloud, null);
    }

    public static boolean applyLingering(ServerPlayer player, AreaEffectCloud cloud, ItemStack source) {
        if (player == null || cloud == null) {
            return false;
        }
        FabricGraniteSkills plugin = FabricGraniteSkills.getInstance();
        if (plugin == null) {
            return false;
        }
        FabricAlchemyAbilities self = plugin.getAbilityManager().getAbilityImpl(FabricAlchemyAbilities.class);
        var ability = Abilities.LINGERING;
        if (self.isDisabled(ability) || self.failsChecks(player, ability)) {
            return false;
        }
        if (source != null) {
            PotionContents contents = source.get(DataComponents.POTION_CONTENTS);
            if (contents != null
                    && !contents.customEffects().isEmpty()
                    && Skills.ALCHEMY.optionBoolean("ignore_custom_potions", false)) {
                return false;
            }
        }
        User user = plugin.getUserManager().getUser(player.getUUID());
        double naturalDecay = 1.0 - (self.getValue(ability, user) / 100.0);
        double entityDecay = 1.0 - (self.getSecondaryValue(ability, user) / 100.0);
        if (naturalDecay <= 0.01) {
            naturalDecay = 0.01;
        }
        if (entityDecay <= 0.01) {
            entityDecay = 0.01;
        }
        cloud.setRadiusPerTick((float) (cloud.getRadiusPerTick() * naturalDecay));
        cloud.setRadiusOnUse((float) (cloud.getRadiusOnUse() * entityDecay));
        return true;
    }

    public static void onKill(FabricGraniteSkills plugin, ServerPlayer player, LivingEntity victim) {
        if (player == null || victim == null || player == victim) {
            return;
        }
        boolean hostile = victim instanceof Enemy || victim instanceof ServerPlayer
                || victim.getType() == net.minecraft.world.entity.EntityTypes.PHANTOM;
        if (!hostile || player.getHealth() <= 0f) {
            return;
        }
        FabricAlchemyAbilities self = plugin.getAbilityManager().getAbilityImpl(FabricAlchemyAbilities.class);
        var ability = Abilities.LIFE_STEAL;
        if (self.isDisabled(ability) || self.failsChecks(player, ability)) {
            return;
        }
        User user = plugin.getUserManager().getUser(player.getUUID());
        double healthRegen = victim.getMaxHealth() * (self.getValue(ability, user) / 100.0);
        float max = player.getMaxHealth();
        player.setHealth((float) Math.min(max, player.getHealth() + healthRegen));
    }

    /** Probe entry: refresh Wise Effect for all online players. */
    public static void refreshWiseEffectNow(FabricGraniteSkills plugin) {
        if (plugin == null) {
            return;
        }
        plugin.getAbilityManager().getAbilityImpl(FabricAlchemyAbilities.class).refreshWiseEffect();
    }

    /** Probe entry: refresh Wise Effect for one player (does not require online list membership). */
    public static void refreshWiseEffectFor(ServerPlayer player) {
        FabricGraniteSkills plugin = FabricGraniteSkills.getInstance();
        if (plugin == null || player == null) {
            return;
        }
        plugin.getAbilityManager().getAbilityImpl(FabricAlchemyAbilities.class).refreshWiseEffectForPlayer(player);
    }

    private void refreshWiseEffect() {
        if (plugin.getServer() == null) {
            return;
        }
        for (ServerPlayer player : plugin.getServer().getPlayerList().getPlayers()) {
            refreshWiseEffectForPlayer(player);
        }
    }

    private void refreshWiseEffectForPlayer(ServerPlayer player) {
        var ability = Abilities.WISE_EFFECT;
        User user = plugin.getUserManager().getUser(player.getUUID());
        if (user == null) {
            return;
        }
        if (player.getActiveEffects().isEmpty() || isDisabled(ability) || failsChecks(player, ability)) {
            user.removeStatModifier(WISE_EFFECT);
            return;
        }
        Set<Object> types = new HashSet<>();
        player.getActiveEffects().forEach(effect -> types.add(effect.getEffect()));
        double modifier = getValue(ability, user) * types.size();
        if (modifier > 0) {
            user.addStatModifier(new StatModifier(WISE_EFFECT, Stats.WISDOM, modifier, Operation.ADD), true);
        } else {
            user.removeStatModifier(WISE_EFFECT);
        }
    }
}
