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
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
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
    public static void onMainHandShieldSwing(FabricAuraSkills plugin, ServerPlayer player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND || !player.getMainHandItem().is(Items.SHIELD)) {
            return;
        }
        if (!(player.level() instanceof ServerLevel sw)) {
            return;
        }
        Vec3 eye = player.getEyePosition();
        double reach = player.blockInteractionRange();
        Vec3 rot = player.getViewVector(1.0f);
        Vec3 end = eye.add(rot.x() * reach, rot.y() * reach, rot.z() * reach);
        BlockHitResult hit = sw.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, player));
        BlockPos pos;
        BlockState state;
        if (hit.getType() == HitResult.Type.BLOCK) {
            pos = hit.getBlockPos();
            state = sw.getBlockState(pos);
        } else {
            pos = BlockPos.containing(player.getX(), player.getY(), player.getZ());
            state = sw.getBlockState(pos);
        }
        tryReadyOnAttackBlock(plugin, player, sw, pos, state);
    }

    public static void register(FabricAuraSkills plugin) {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((LivingEntity entity, DamageSource source, float amount) ->
                allowDamage(plugin, entity, amount));
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (world.isClientSide() || !(player instanceof ServerPlayer sp) || !(world instanceof ServerLevel sw)) {
                return InteractionResult.PASS;
            }
            if (hand != InteractionHand.MAIN_HAND) {
                return InteractionResult.PASS;
            }
            tryReadyOnAttackBlock(plugin, sp, sw, pos, sw.getBlockState(pos));
            return InteractionResult.PASS;
        });
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.player;
            plugin.getScheduler().scheduleSync(() -> joinCleanup(plugin, player), 50L, TimeUnit.MILLISECONDS);
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> stopIfActive(plugin, handler.player));
    }

    private static boolean allowDamage(FabricAuraSkills plugin, LivingEntity entity, float amount) {
        if (!(entity instanceof ServerPlayer player)) {
            return true;
        }
        if (amount <= 0f) {
            return true;
        }
        if (isDisabled()) {
            return true;
        }
        if (!plugin.getUserManager().hasUser(player.getUUID())) {
            return true;
        }
        User user = plugin.getUserManager().getUser(player.getUUID());
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
    private static boolean tryAbsorbHit(FabricAuraSkills plugin, ServerPlayer player, User user, float amount) {
        double mana = user.getMana() - amount * 2.0;
        if (mana <= 0) {
            return false;
        }
        user.setMana(mana);
        if (player.level() instanceof ServerLevel world) {
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.GUARDIAN_HURT, SoundSource.PLAYERS, 1.0f, 1.0f);
            if (MANA.optionBoolean("enable_particles", true)) {
                double x = player.getX();
                double y = player.getY() + 1.0;
                double z = player.getZ();
                double radius = 1.0;
                double rate = Math.PI / 20.0;
                for (double theta = 0; theta <= Math.PI * 2; theta += rate) {
                    double px = x + radius * Math.cos(theta);
                    double pz = z + radius * Math.sin(theta);
                    world.sendParticles(ParticleTypes.WITCH, px, y, pz, 1, 0.0, 0.0, 0.0, 0.0);
                }
            }
        }
        return true;
    }

    private static void tryReadyOnAttackBlock(FabricAuraSkills plugin, ServerPlayer player, ServerLevel world,
            BlockPos pos, BlockState stateAtPos) {
        if (isDisabled()) {
            return;
        }
        if (!player.getMainHandItem().is(Items.SHIELD)) {
            return;
        }
        User user = plugin.getUserManager().getUser(player.getUUID());
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

    private static void scheduleUnready(FabricAuraSkills plugin, ServerPlayer player, Locale locale, ManaAbilityData data) {
        int readyDurationTicks = 80;
        plugin.getScheduler().scheduleSync(() -> {
            if (!plugin.getUserManager().hasUser(player.getUUID())) {
                return;
            }
            User user = plugin.getUserManager().getUser(player.getUUID());
            ManaAbilityData d = user.getManaAbilityData(MANA);
            if (!d.isActivated() && d.isReady()) {
                d.setReady(false);
                plugin.getAbilityManager().sendAbilityMessage(user, plugin.getMsg(ManaAbilityMessage.ABSORPTION_LOWER, locale));
            }
        }, readyDurationTicks * 50L, TimeUnit.MILLISECONDS);
    }

    private static boolean isAllowReady(FabricAuraSkills plugin, ServerPlayer player, ServerLevel world, BlockPos pos) {
        if (MANA.optionBoolean("require_sneak", false) && !player.isShiftKeyDown()) {
            return false;
        }
        if (isBlockPlace(player, world, pos)) {
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
        net.minecraft.world.item.ItemStack off = player.getOffhandItem();
        if (off.isEmpty()) {
            return false;
        }
        if (player.isShiftKeyDown() && MANA.optionBoolean("sneak_offhand_bypass", true)) {
            return false;
        }
        return off.getItem() instanceof net.minecraft.world.item.BlockItem;
    }

    private static boolean checkActivation(FabricAuraSkills plugin, ServerPlayer player, User user) {
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
        if (player.level() instanceof ServerLevel soundWorld) {
            soundWorld.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1.0f, 1.0f);
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

    private static void stop(FabricAuraSkills plugin, ServerPlayer player) {
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
        user.getAbilityData(MANA).setData(ABSORPTION_ACTIVE_FLAG, false);
        data.setCooldown((int) MANA.getCooldown(user.getManaAbilityLevel(MANA)) * 20);
        user.sendMessage(plugin.getMsg(ManaAbilityMessage.ABSORPTION_END, user.getLocale()));
    }

    private static void stopIfActive(FabricAuraSkills plugin, ServerPlayer player) {
        if (!plugin.getUserManager().hasUser(player.getUUID())) {
            return;
        }
        ManaAbilityData data = plugin.getUserManager().getUser(player.getUUID()).getManaAbilityData(MANA);
        if (data.isActivated()) {
            stop(plugin, player);
        }
    }

    private static void joinCleanup(FabricAuraSkills plugin, ServerPlayer player) {
        if (!plugin.getUserManager().hasUser(player.getUUID())) {
            return;
        }
        User user = plugin.getUserManager().getUser(player.getUUID());
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

    private static boolean failsAbsorptionContext(FabricAuraSkills plugin, ServerPlayer player, User user) {
        if (plugin.configBoolean(Option.DISABLE_IN_CREATIVE_MODE) && player.getAbilities().instabuild) {
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
