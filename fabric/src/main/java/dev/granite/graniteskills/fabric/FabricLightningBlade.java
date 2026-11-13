package dev.granite.graniteskills.fabric;

import dev.granite.graniteskills.api.mana.ManaAbilities;
import dev.granite.graniteskills.api.mana.ManaAbility;
import dev.granite.graniteskills.api.util.NumberUtil;
import dev.granite.graniteskills.common.config.Option;
import dev.granite.graniteskills.common.mana.ManaAbilityData;
import dev.granite.graniteskills.common.message.type.ManaAbilityMessage;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.common.util.text.TextUtil;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Fabric port of Bukkit {@link dev.granite.graniteskills.bukkit.skills.fighting.LightningBlade}: readied right-click with a
 * sword, then the next main-hand melee hit on a living entity consumes mana and applies a temporary attack speed boost.
 * Sweep damage to secondary targets does not go through {@link AttackEntityCallback}, matching Paper primary-hit-only
 * {@code EntityDamageByEntityEvent} behavior for this ability.
 */
public final class FabricLightningBlade {

    private static final ManaAbility MANA = ManaAbilities.LIGHTNING_BLADE;
    private static final Identifier MODIFIER_ID = Identifier.fromNamespaceAndPath("graniteskills", "lightning_blade_mana_ability");

    private FabricLightningBlade() {
    }

    public static void register(FabricGraniteSkills plugin) {
        registerReadyListeners(plugin);
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClientSide() || !(player instanceof ServerPlayer sp)) {
                return InteractionResult.PASS;
            }
            if (hand != InteractionHand.MAIN_HAND) {
                return InteractionResult.PASS;
            }
            if (!(entity instanceof LivingEntity living) || living == player) {
                return InteractionResult.PASS;
            }
            if (!sp.getMainHandItem().is(ItemTags.SWORDS)) {
                return InteractionResult.PASS;
            }
            onMeleeAttack(plugin, sp, living);
            return InteractionResult.PASS;
        });
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.player;
            plugin.getScheduler().scheduleSync(() -> joinCleanup(plugin, player), 50L, TimeUnit.MILLISECONDS);
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> stopIfActive(plugin, handler.player));
    }

    private static void onMeleeAttack(FabricGraniteSkills plugin, ServerPlayer player, LivingEntity victim) {
        if (isDisabled()) {
            return;
        }
        if (!plugin.getUserManager().hasUser(player.getUUID())) {
            return;
        }
        User user = plugin.getUserManager().getUser(player.getUUID());
        if (user.getManaAbilityLevel(MANA) <= 0) {
            return;
        }
        if (failsActivationContext(plugin, player, user)) {
            return;
        }
        ManaAbilityData data = user.getManaAbilityData(MANA);
        if (data.isActivated()) {
            return;
        }
        tryActivate(plugin, player, user);
    }

    private static void registerReadyListeners(FabricGraniteSkills plugin) {
        UseBlockCallback.EVENT.register((Player player, Level world, InteractionHand hand, BlockHitResult hitResult) -> {
            if (world.isClientSide() || !(player instanceof ServerPlayer sp) || !(world instanceof ServerLevel sw)) {
                return InteractionResult.PASS;
            }
            if (hand != InteractionHand.MAIN_HAND) {
                return InteractionResult.PASS;
            }
            tryReady(plugin, sp, sw, hitResult.getBlockPos(), sw.getBlockState(hitResult.getBlockPos()));
            return InteractionResult.PASS;
        });

        UseItemCallback.EVENT.register((Player player, Level world, InteractionHand hand) -> {
            if (world.isClientSide() || !(player instanceof ServerPlayer sp)) {
                return InteractionResult.PASS;
            }
            if (hand != InteractionHand.MAIN_HAND) {
                return InteractionResult.PASS;
            }
            tryReady(plugin, sp, null, null, null);
            return InteractionResult.PASS;
        });
    }

    private static void tryReady(FabricGraniteSkills plugin, ServerPlayer player, @Nullable ServerLevel world,
            @Nullable BlockPos pos, @Nullable BlockState stateAtPos) {
        if (isDisabled()) {
            return;
        }
        if (!holdingSword(player)) {
            return;
        }
        if (!plugin.getUserManager().hasUser(player.getUUID())) {
            return;
        }
        User user = plugin.getUserManager().getUser(player.getUUID());
        if (user.getManaAbilityLevel(MANA) <= 0) {
            return;
        }
        if (world != null && pos != null && stateAtPos != null) {
            if (FabricReplenish.blocksReadiedActivate(stateAtPos, world, pos)) {
                return;
            }
        }
        if (!isAllowReady(plugin, player, world, pos)) {
            return;
        }

        ManaAbilityData data = user.getManaAbilityData(MANA);
        Locale locale = user.getLocale();
        if (data.isActivated() || data.isReady()) {
            return;
        }
        if (data.getCooldown() == 0) {
            data.setReady(true);
            plugin.getAbilityManager().sendAbilityMessage(user, plugin.getMsg(ManaAbilityMessage.LIGHTNING_BLADE_RAISE, locale));
            scheduleUnready(plugin, player, locale, data);
        } else {
            if (data.getErrorTimer() == 0) {
                String msg = TextUtil.replace(plugin.getMsg(ManaAbilityMessage.NOT_READY, locale), "{cooldown}",
                        NumberUtil.format0((double) data.getCooldown() / 20.0));
                user.sendMessage(msg);
                data.setErrorTimer(2);
            }
        }
    }

    private static void scheduleUnready(FabricGraniteSkills plugin, ServerPlayer player, Locale locale, ManaAbilityData data) {
        int readyDurationTicks = 80;
        plugin.getScheduler().scheduleSync(() -> {
            if (!plugin.getUserManager().hasUser(player.getUUID())) {
                return;
            }
            User user = plugin.getUserManager().getUser(player.getUUID());
            ManaAbilityData d = user.getManaAbilityData(MANA);
            if (!d.isActivated() && d.isReady()) {
                d.setReady(false);
                plugin.getAbilityManager().sendAbilityMessage(user, plugin.getMsg(ManaAbilityMessage.LIGHTNING_BLADE_LOWER, locale));
            }
        }, readyDurationTicks * 50L, TimeUnit.MILLISECONDS);
    }

    private static boolean isAllowReady(FabricGraniteSkills plugin, ServerPlayer player, @Nullable ServerLevel world,
            @Nullable BlockPos pos) {
        if (MANA.optionBoolean("require_sneak", false) && !player.isShiftKeyDown()) {
            return false;
        }
        if (world != null && pos != null && isBlockPlace(player, world, pos)) {
            return false;
        }
        String w = FabricUser.worldNameCompat(player);
        if (plugin.getWorldManager().isDisabledWorld(w)) {
            return false;
        }
        User user = plugin.getUserManager().getUser(player.getUUID());
        return user.hasSkillPermission(MANA.getSkill());
    }

    private static boolean isBlockPlace(ServerPlayer player, ServerLevel world, BlockPos pos) {
        if (!MANA.optionBoolean("check_offhand", true)) {
            return false;
        }
        ItemStack off = player.getOffhandItem();
        if (off.isEmpty()) {
            return false;
        }
        if (player.isShiftKeyDown() && MANA.optionBoolean("sneak_offhand_bypass", true)) {
            return false;
        }
        return off.getItem() instanceof BlockItem;
    }

    private static boolean tryActivate(FabricGraniteSkills plugin, ServerPlayer player, User user) {
        ManaAbilityData data = user.getManaAbilityData(MANA);
        if (!data.isReady() || data.isActivated()) {
            return false;
        }
        int durationTicks = durationTicks(user);
        double manaCost = MANA.getManaCost(user.getManaAbilityLevel(MANA));
        if (insufficientMana(plugin, user, manaCost)) {
            return false;
        }

        data.setActivated(true);
        applyAttackSpeedModifier(player, user);
        if (player.level() instanceof ServerLevel soundWorld) {
            soundWorld.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ILLUSIONER_PREPARE_MIRROR, SoundSource.PLAYERS, 0.5f, 1.0f);
        }
        double manaUsed = consumeMana(plugin, player, user, manaCost);
        FabricManaAbilityUseXpHandler.onManaAbilityActivated(player, MANA, manaUsed);

        if (durationTicks != 0) {
            plugin.getScheduler().scheduleSync(() -> stop(plugin, player), durationTicks * 50L, TimeUnit.MILLISECONDS);
        } else {
            stop(plugin, player);
        }
        return true;
    }

    private static int durationTicks(User user) {
        double baseDuration = MANA.optionDouble("base_duration", 5.0);
        double durationPerLevel = MANA.optionDouble("duration_per_level", 4.0);
        double durationSeconds = baseDuration + durationPerLevel * (user.getManaAbilityLevel(MANA) - 1);
        return (int) Math.round(durationSeconds * 20.0);
    }

    private static void applyAttackSpeedModifier(ServerPlayer player, User user) {
        AttributeInstance inst = player.getAttribute(Attributes.ATTACK_SPEED);
        if (inst == null) {
            return;
        }
        inst.removeModifier(MODIFIER_ID);
        double addedValue = MANA.getValue(user.getManaAbilityLevel(MANA)) / 100.0;
        inst.addPermanentModifier(new AttributeModifier(MODIFIER_ID, addedValue, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }

    private static void removeAttackSpeedModifier(ServerPlayer player) {
        AttributeInstance inst = player.getAttribute(Attributes.ATTACK_SPEED);
        if (inst != null) {
            inst.removeModifier(MODIFIER_ID);
        }
    }

    private static void stop(FabricGraniteSkills plugin, ServerPlayer player) {
        if (!plugin.getUserManager().hasUser(player.getUUID())) {
            return;
        }
        User user = plugin.getUserManager().getUser(player.getUUID());
        ManaAbilityData data = user.getManaAbilityData(MANA);
        if (!data.isActivated()) {
            return;
        }
        data.setActivated(false);
        data.setReady(false);
        data.setCooldown((int) MANA.getCooldown(user.getManaAbilityLevel(MANA)) * 20);
        removeAttackSpeedModifier(player);
        user.sendMessage(plugin.getMsg(ManaAbilityMessage.LIGHTNING_BLADE_END, user.getLocale()));
    }

    private static void stopIfActive(FabricGraniteSkills plugin, ServerPlayer player) {
        if (!plugin.getUserManager().hasUser(player.getUUID())) {
            return;
        }
        if (plugin.getUserManager().getUser(player.getUUID()).getManaAbilityData(MANA).isActivated()) {
            stop(plugin, player);
        }
    }

    private static void joinCleanup(FabricGraniteSkills plugin, ServerPlayer player) {
        if (!plugin.getUserManager().hasUser(player.getUUID())) {
            return;
        }
        User user = plugin.getUserManager().getUser(player.getUUID());
        if (!user.getManaAbilityData(MANA).isActivated()) {
            removeAttackSpeedModifier(player);
        }
    }

    private static double consumeMana(FabricGraniteSkills plugin, ServerPlayer player, User user, double manaCost) {
        double manaConsumed;
        if (plugin.configBoolean(Option.MANA_ENABLED)) {
            manaConsumed = manaCost;
            user.setMana(user.getMana() - manaConsumed);
        } else {
            manaConsumed = 0.0;
        }
        String msg = TextUtil.replace(plugin.getMsg(ManaAbilityMessage.LIGHTNING_BLADE_START, user.getLocale()),
                "{mana}", NumberUtil.format0(manaConsumed));
        user.sendMessage(msg);
        return manaConsumed;
    }

    private static boolean insufficientMana(FabricGraniteSkills plugin, User user, double manaCost) {
        if (!plugin.configBoolean(Option.MANA_ENABLED) || user.getMana() >= manaCost) {
            return false;
        }
        plugin.getManaAbilityManager().sendNotEnoughManaMessage(user, manaCost);
        return true;
    }

    private static boolean failsActivationContext(FabricGraniteSkills plugin, ServerPlayer player, User user) {
        String w = FabricUser.worldNameCompat(player);
        if (plugin.getWorldManager().isDisabledWorld(w)) {
            return true;
        }
        if (!user.hasSkillPermission(MANA.getSkill())) {
            return true;
        }
        return plugin.configBoolean(Option.DISABLE_IN_CREATIVE_MODE) && player.getAbilities().instabuild;
    }

    private static boolean isDisabled() {
        return !MANA.isEnabled() || !MANA.getSkill().isEnabled();
    }

    private static boolean holdingSword(ServerPlayer player) {
        return player.getMainHandItem().is(ItemTags.SWORDS);
    }
}
