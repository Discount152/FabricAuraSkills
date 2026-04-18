package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.api.mana.ManaAbilities;
import dev.aurelium.auraskills.api.mana.ManaAbility;
import dev.aurelium.auraskills.api.util.NumberUtil;
import dev.aurelium.auraskills.common.config.Option;
import dev.aurelium.auraskills.common.mana.ManaAbilityData;
import dev.aurelium.auraskills.common.message.type.ManaAbilityMessage;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.common.util.text.TextUtil;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Fabric port of Bukkit {@link dev.aurelium.auraskills.bukkit.skills.fighting.LightningBlade}: readied right-click with a
 * sword, then the next main-hand melee hit on a living entity consumes mana and applies a temporary attack speed boost.
 * Sweep damage to secondary targets does not go through {@link AttackEntityCallback}, matching Paper primary-hit-only
 * {@code EntityDamageByEntityEvent} behavior for this ability.
 */
public final class FabricLightningBlade {

    private static final ManaAbility MANA = ManaAbilities.LIGHTNING_BLADE;
    private static final Identifier MODIFIER_ID = Identifier.of("auraskills", "lightning_blade_mana_ability");

    private FabricLightningBlade() {
    }

    public static void register(FabricAuraSkills plugin) {
        registerReadyListeners(plugin);
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient() || !(player instanceof ServerPlayerEntity sp)) {
                return ActionResult.PASS;
            }
            if (hand != Hand.MAIN_HAND) {
                return ActionResult.PASS;
            }
            if (!(entity instanceof LivingEntity living) || living == player) {
                return ActionResult.PASS;
            }
            if (!sp.getMainHandStack().isIn(ItemTags.SWORDS)) {
                return ActionResult.PASS;
            }
            onMeleeAttack(plugin, sp, living);
            return ActionResult.PASS;
        });
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.player;
            plugin.getScheduler().scheduleSync(() -> joinCleanup(plugin, player), 50L, TimeUnit.MILLISECONDS);
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> stopIfActive(plugin, handler.player));
    }

    private static void onMeleeAttack(FabricAuraSkills plugin, ServerPlayerEntity player, LivingEntity victim) {
        if (isDisabled()) {
            return;
        }
        if (!plugin.getUserManager().hasUser(player.getUuid())) {
            return;
        }
        User user = plugin.getUserManager().getUser(player.getUuid());
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

    private static void registerReadyListeners(FabricAuraSkills plugin) {
        UseBlockCallback.EVENT.register((PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) -> {
            if (world.isClient() || !(player instanceof ServerPlayerEntity sp) || !(world instanceof ServerWorld sw)) {
                return ActionResult.PASS;
            }
            if (hand != Hand.MAIN_HAND) {
                return ActionResult.PASS;
            }
            tryReady(plugin, sp, sw, hitResult.getBlockPos(), sw.getBlockState(hitResult.getBlockPos()));
            return ActionResult.PASS;
        });

        UseItemCallback.EVENT.register((PlayerEntity player, World world, Hand hand) -> {
            if (world.isClient() || !(player instanceof ServerPlayerEntity sp)) {
                return ActionResult.PASS;
            }
            if (hand != Hand.MAIN_HAND) {
                return ActionResult.PASS;
            }
            tryReady(plugin, sp, null, null, null);
            return ActionResult.PASS;
        });
    }

    private static void tryReady(FabricAuraSkills plugin, ServerPlayerEntity player, @Nullable ServerWorld world,
            @Nullable BlockPos pos, @Nullable BlockState stateAtPos) {
        if (isDisabled()) {
            return;
        }
        if (!holdingSword(player)) {
            return;
        }
        if (!plugin.getUserManager().hasUser(player.getUuid())) {
            return;
        }
        User user = plugin.getUserManager().getUser(player.getUuid());
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

    private static void scheduleUnready(FabricAuraSkills plugin, ServerPlayerEntity player, Locale locale, ManaAbilityData data) {
        int readyDurationTicks = 80;
        plugin.getScheduler().scheduleSync(() -> {
            if (!plugin.getUserManager().hasUser(player.getUuid())) {
                return;
            }
            User user = plugin.getUserManager().getUser(player.getUuid());
            ManaAbilityData d = user.getManaAbilityData(MANA);
            if (!d.isActivated() && d.isReady()) {
                d.setReady(false);
                plugin.getAbilityManager().sendAbilityMessage(user, plugin.getMsg(ManaAbilityMessage.LIGHTNING_BLADE_LOWER, locale));
            }
        }, readyDurationTicks * 50L, TimeUnit.MILLISECONDS);
    }

    private static boolean isAllowReady(FabricAuraSkills plugin, ServerPlayerEntity player, @Nullable ServerWorld world,
            @Nullable BlockPos pos) {
        if (MANA.optionBoolean("require_sneak", false) && !player.isSneaking()) {
            return false;
        }
        if (world != null && pos != null && isBlockPlace(player, world, pos)) {
            return false;
        }
        String w = FabricUser.worldNameCompat(player);
        if (plugin.getWorldManager().isDisabledWorld(w)) {
            return false;
        }
        User user = plugin.getUserManager().getUser(player.getUuid());
        return user.hasSkillPermission(MANA.getSkill());
    }

    private static boolean isBlockPlace(ServerPlayerEntity player, ServerWorld world, BlockPos pos) {
        if (!MANA.optionBoolean("check_offhand", true)) {
            return false;
        }
        ItemStack off = player.getOffHandStack();
        if (off.isEmpty()) {
            return false;
        }
        if (player.isSneaking() && MANA.optionBoolean("sneak_offhand_bypass", true)) {
            return false;
        }
        return off.getItem() instanceof BlockItem;
    }

    private static boolean tryActivate(FabricAuraSkills plugin, ServerPlayerEntity player, User user) {
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
        if (player.getEntityWorld() instanceof ServerWorld soundWorld) {
            soundWorld.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENTITY_ILLUSIONER_PREPARE_MIRROR, SoundCategory.PLAYERS, 0.5f, 1.0f);
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

    private static void applyAttackSpeedModifier(ServerPlayerEntity player, User user) {
        EntityAttributeInstance inst = player.getAttributeInstance(EntityAttributes.ATTACK_SPEED);
        if (inst == null) {
            return;
        }
        inst.removeModifier(MODIFIER_ID);
        double addedValue = MANA.getValue(user.getManaAbilityLevel(MANA)) / 100.0;
        inst.addPersistentModifier(new EntityAttributeModifier(MODIFIER_ID, addedValue, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }

    private static void removeAttackSpeedModifier(ServerPlayerEntity player) {
        EntityAttributeInstance inst = player.getAttributeInstance(EntityAttributes.ATTACK_SPEED);
        if (inst != null) {
            inst.removeModifier(MODIFIER_ID);
        }
    }

    private static void stop(FabricAuraSkills plugin, ServerPlayerEntity player) {
        if (!plugin.getUserManager().hasUser(player.getUuid())) {
            return;
        }
        User user = plugin.getUserManager().getUser(player.getUuid());
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

    private static void stopIfActive(FabricAuraSkills plugin, ServerPlayerEntity player) {
        if (!plugin.getUserManager().hasUser(player.getUuid())) {
            return;
        }
        if (plugin.getUserManager().getUser(player.getUuid()).getManaAbilityData(MANA).isActivated()) {
            stop(plugin, player);
        }
    }

    private static void joinCleanup(FabricAuraSkills plugin, ServerPlayerEntity player) {
        if (!plugin.getUserManager().hasUser(player.getUuid())) {
            return;
        }
        User user = plugin.getUserManager().getUser(player.getUuid());
        if (!user.getManaAbilityData(MANA).isActivated()) {
            removeAttackSpeedModifier(player);
        }
    }

    private static double consumeMana(FabricAuraSkills plugin, ServerPlayerEntity player, User user, double manaCost) {
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

    private static boolean insufficientMana(FabricAuraSkills plugin, User user, double manaCost) {
        if (!plugin.configBoolean(Option.MANA_ENABLED) || user.getMana() >= manaCost) {
            return false;
        }
        plugin.getManaAbilityManager().sendNotEnoughManaMessage(user, manaCost);
        return true;
    }

    private static boolean failsActivationContext(FabricAuraSkills plugin, ServerPlayerEntity player, User user) {
        String w = FabricUser.worldNameCompat(player);
        if (plugin.getWorldManager().isDisabledWorld(w)) {
            return true;
        }
        if (!user.hasSkillPermission(MANA.getSkill())) {
            return true;
        }
        return plugin.configBoolean(Option.DISABLE_IN_CREATIVE_MODE) && player.getAbilities().creativeMode;
    }

    private static boolean isDisabled() {
        return !MANA.isEnabled() || !MANA.getSkill().isEnabled();
    }

    private static boolean holdingSword(ServerPlayerEntity player) {
        return player.getMainHandStack().isIn(ItemTags.SWORDS);
    }
}
