package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.api.mana.ManaAbilities;
import dev.aurelium.auraskills.api.mana.ManaAbility;
import dev.aurelium.auraskills.api.util.NumberUtil;
import dev.aurelium.auraskills.common.config.Option;
import dev.aurelium.auraskills.common.mana.ManaAbilityData;
import dev.aurelium.auraskills.common.message.type.ManaAbilityMessage;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.common.util.text.TextUtil;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Fabric port of Bukkit {@link dev.aurelium.auraskills.bukkit.skills.defense.Absorption}: raise with shield, then incoming
 * hits consume mana ({@code damage * 2}) to negate damage. Ready state uses {@link AttackBlockCallback} (left-click block)
 * plus {@link dev.aurelium.auraskills.fabric.mixin.ServerPlayerEntityShieldAbsorptionSwingMixin} for main-hand shield
 * swings (including air), matching Bukkit {@code LEFT_CLICK_AIR} / {@code LEFT_CLICK_BLOCK} readiness.
 */
public final class FabricAbsorption {

    private static final ManaAbility MANA = ManaAbilities.ABSORPTION;
    private static final String ABSORPTION_ACTIVE_FLAG = "activated";

    private FabricAbsorption() {
    }

    /**
     * Called from {@link dev.aurelium.auraskills.fabric.mixin.ServerPlayerEntityShieldAbsorptionSwingMixin} when the
     * player swings the main hand while holding a shield (including left-click air).
     */
    public static void onMainHandShieldSwing(FabricAuraSkills plugin, ServerPlayerEntity player, Hand hand) {
        if (hand != Hand.MAIN_HAND || !player.getMainHandStack().isOf(Items.SHIELD)) {
            return;
        }
        if (!(player.getEntityWorld() instanceof ServerWorld sw)) {
            return;
        }
        Vec3d eye = player.getEyePos();
        double reach = player.getBlockInteractionRange();
        Vec3d rot = player.getRotationVec(1.0f);
        Vec3d end = eye.add(rot.getX() * reach, rot.getY() * reach, rot.getZ() * reach);
        BlockHitResult hit = sw.raycast(new RaycastContext(eye, end, RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE, player));
        BlockPos pos;
        BlockState state;
        if (hit.getType() == HitResult.Type.BLOCK) {
            pos = hit.getBlockPos();
            state = sw.getBlockState(pos);
        } else {
            pos = BlockPos.ofFloored(player.getX(), player.getY(), player.getZ());
            state = sw.getBlockState(pos);
        }
        tryReadyOnAttackBlock(plugin, player, sw, pos, state);
    }

    public static void register(FabricAuraSkills plugin) {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((LivingEntity entity, DamageSource source, float amount) ->
                allowDamage(plugin, entity, amount));
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (world.isClient() || !(player instanceof ServerPlayerEntity sp) || !(world instanceof ServerWorld sw)) {
                return ActionResult.PASS;
            }
            if (hand != Hand.MAIN_HAND) {
                return ActionResult.PASS;
            }
            tryReadyOnAttackBlock(plugin, sp, sw, pos, sw.getBlockState(pos));
            return ActionResult.PASS;
        });
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.player;
            plugin.getScheduler().scheduleSync(() -> joinCleanup(plugin, player), 50L, TimeUnit.MILLISECONDS);
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> stopIfActive(plugin, handler.player));
    }

    private static boolean allowDamage(FabricAuraSkills plugin, LivingEntity entity, float amount) {
        if (!(entity instanceof ServerPlayerEntity player)) {
            return true;
        }
        if (amount <= 0f) {
            return true;
        }
        if (isDisabled()) {
            return true;
        }
        if (!plugin.getUserManager().hasUser(player.getUuid())) {
            return true;
        }
        User user = plugin.getUserManager().getUser(player.getUuid());
        if (user.getManaAbilityLevel(MANA) <= 0) {
            return true;
        }
        if (failsAbsorptionContext(plugin, player, user)) {
            return true;
        }
        boolean flag = user.getAbilityData(MANA).getBoolean(ABSORPTION_ACTIVE_FLAG);
        boolean manaActivated = user.getManaAbilityData(MANA).isActivated();
        if (flag && manaActivated) {
            return !tryAbsorbHit(plugin, player, user, amount);
        }
        if (checkActivation(plugin, player, user)) {
            return !tryAbsorbHit(plugin, player, user, amount);
        }
        return true;
    }

    /**
     * @return {@code true} if damage was fully absorbed (caller should cancel damage).
     */
    private static boolean tryAbsorbHit(FabricAuraSkills plugin, ServerPlayerEntity player, User user, float amount) {
        double mana = user.getMana() - amount * 2.0;
        if (mana <= 0) {
            return false;
        }
        user.setMana(mana);
        if (player.getEntityWorld() instanceof ServerWorld world) {
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENTITY_GUARDIAN_HURT, SoundCategory.PLAYERS, 1.0f, 1.0f);
            if (MANA.optionBoolean("enable_particles", true)) {
                double x = player.getX();
                double y = player.getY() + 1.0;
                double z = player.getZ();
                double radius = 1.0;
                double rate = Math.PI / 20.0;
                for (double theta = 0; theta <= Math.PI * 2; theta += rate) {
                    double px = x + radius * Math.cos(theta);
                    double pz = z + radius * Math.sin(theta);
                    world.spawnParticles(ParticleTypes.WITCH, px, y, pz, 1, 0.0, 0.0, 0.0, 0.0);
                }
            }
        }
        return true;
    }

    private static void tryReadyOnAttackBlock(FabricAuraSkills plugin, ServerPlayerEntity player, ServerWorld world,
            BlockPos pos, BlockState stateAtPos) {
        if (isDisabled()) {
            return;
        }
        if (!player.getMainHandStack().isOf(Items.SHIELD)) {
            return;
        }
        User user = plugin.getUserManager().getUser(player.getUuid());
        if (failsAbsorptionContext(plugin, player, user)) {
            return;
        }
        if (user.getManaAbilityLevel(MANA) <= 0) {
            return;
        }
        if (FabricReplenish.blocksReadiedActivate(stateAtPos, world, pos)) {
            return;
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
            plugin.getAbilityManager().sendAbilityMessage(user, plugin.getMsg(ManaAbilityMessage.ABSORPTION_RAISE, locale));
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
                plugin.getAbilityManager().sendAbilityMessage(user, plugin.getMsg(ManaAbilityMessage.ABSORPTION_LOWER, locale));
            }
        }, readyDurationTicks * 50L, TimeUnit.MILLISECONDS);
    }

    private static boolean isAllowReady(FabricAuraSkills plugin, ServerPlayerEntity player, ServerWorld world, BlockPos pos) {
        if (MANA.optionBoolean("require_sneak", false) && !player.isSneaking()) {
            return false;
        }
        if (isBlockPlace(player, world, pos)) {
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
        net.minecraft.item.ItemStack off = player.getOffHandStack();
        if (off.isEmpty()) {
            return false;
        }
        if (player.isSneaking() && MANA.optionBoolean("sneak_offhand_bypass", true)) {
            return false;
        }
        return off.getItem() instanceof net.minecraft.item.BlockItem;
    }

    private static boolean checkActivation(FabricAuraSkills plugin, ServerPlayerEntity player, User user) {
        ManaAbilityData data = user.getManaAbilityData(MANA);
        if (!data.isReady() || data.isActivated()) {
            return false;
        }
        int durationTicks = (int) Math.round(MANA.getValue(user.getManaAbilityLevel(MANA)) * 20.0);
        double manaCost = MANA.getManaCost(user.getManaAbilityLevel(MANA));
        if (insufficientMana(plugin, user, manaCost)) {
            return false;
        }
        data.setActivated(true);
        user.getAbilityData(MANA).setData(ABSORPTION_ACTIVE_FLAG, true);
        if (player.getEntityWorld() instanceof ServerWorld soundWorld) {
            soundWorld.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.PLAYERS, 1.0f, 1.0f);
        }
        double manaUsed = consumeMana(plugin, user, manaCost);
        FabricManaAbilityUseXpHandler.onManaAbilityActivated(player, MANA, manaUsed);
        if (durationTicks != 0) {
            plugin.getScheduler().scheduleSync(() -> stop(plugin, player), durationTicks * 50L, TimeUnit.MILLISECONDS);
        } else {
            stop(plugin, player);
        }
        return true;
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
        user.getAbilityData(MANA).setData(ABSORPTION_ACTIVE_FLAG, false);
        data.setCooldown((int) MANA.getCooldown(user.getManaAbilityLevel(MANA)) * 20);
        user.sendMessage(plugin.getMsg(ManaAbilityMessage.ABSORPTION_END, user.getLocale()));
    }

    private static void stopIfActive(FabricAuraSkills plugin, ServerPlayerEntity player) {
        if (!plugin.getUserManager().hasUser(player.getUuid())) {
            return;
        }
        ManaAbilityData data = plugin.getUserManager().getUser(player.getUuid()).getManaAbilityData(MANA);
        if (data.isActivated()) {
            stop(plugin, player);
        }
    }

    private static void joinCleanup(FabricAuraSkills plugin, ServerPlayerEntity player) {
        if (!plugin.getUserManager().hasUser(player.getUuid())) {
            return;
        }
        User user = plugin.getUserManager().getUser(player.getUuid());
        if (!user.getManaAbilityData(MANA).isActivated()) {
            user.getAbilityData(MANA).remove(ABSORPTION_ACTIVE_FLAG);
        }
    }

    private static double consumeMana(FabricAuraSkills plugin, User user, double manaCost) {
        double manaConsumed;
        if (plugin.configBoolean(Option.MANA_ENABLED)) {
            manaConsumed = manaCost;
            user.setMana(user.getMana() - manaConsumed);
        } else {
            manaConsumed = 0.0;
        }
        String msg = TextUtil.replace(plugin.getMsg(ManaAbilityMessage.ABSORPTION_START, user.getLocale()),
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

    private static boolean failsAbsorptionContext(FabricAuraSkills plugin, ServerPlayerEntity player, User user) {
        if (plugin.configBoolean(Option.DISABLE_IN_CREATIVE_MODE) && player.getAbilities().creativeMode) {
            return true;
        }
        if (!user.hasSkillPermission(MANA.getSkill())) {
            return true;
        }
        String w = FabricUser.worldNameCompat(player);
        return plugin.getWorldManager().isBlockedWorld(w) || plugin.getWorldManager().isDisabledWorld(w);
    }

    private static boolean isDisabled() {
        return !MANA.isEnabled() || !MANA.getSkill().isEnabled();
    }
}
