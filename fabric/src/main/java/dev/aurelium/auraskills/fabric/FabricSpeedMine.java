package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.api.mana.ManaAbilities;
import dev.aurelium.auraskills.api.mana.ManaAbility;
import dev.aurelium.auraskills.api.source.SkillSource;
import dev.aurelium.auraskills.api.source.type.BlockXpSource;
import dev.aurelium.auraskills.api.util.NumberUtil;
import dev.aurelium.auraskills.common.config.Option;
import dev.aurelium.auraskills.common.mana.ManaAbilityData;
import dev.aurelium.auraskills.common.message.type.ManaAbilityMessage;
import dev.aurelium.auraskills.common.source.SourceTag;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.common.util.text.TextUtil;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Fabric port of Bukkit {@link dev.aurelium.auraskills.bukkit.skills.mining.SpeedMine}.
 */
public final class FabricSpeedMine {

    private static final ManaAbility MANA = ManaAbilities.SPEED_MINE;

    private FabricSpeedMine() {
    }

    public static void register(FabricAuraSkills plugin) {
        registerReadyListeners(plugin);
        registerBreakActivation(plugin);
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> stopIfActive(plugin, handler.player));
    }

    private static void registerBreakActivation(FabricAuraSkills plugin) {
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (world.isClientSide() || !(player instanceof ServerPlayer sp) || !(world instanceof ServerLevel)) {
                return true;
            }
            if (plugin.configBoolean(Option.CHECK_BLOCK_REPLACE_ENABLED) && isPlayerPlacedBlock((ServerLevel) world, pos)) {
                return true;
            }
            User user = plugin.getUserManager().getUser(sp.getUUID());
            if (failsBreakActivationChecks(plugin, sp, user)) {
                return true;
            }
            SkillSource<BlockXpSource> skillSource = findSpeedMineBreakSource(plugin, state);
            if (skillSource == null) {
                return true;
            }
            BlockXpSource source = skillSource.source();
            if (!plugin.getSkillManager().hasTag(source, SourceTag.SPEED_MINE_APPLICABLE)) {
                return true;
            }
            if (!holdingPickaxe(sp)) {
                return true;
            }
            tryActivate(plugin, sp, user);
            return true;
        });
    }

    private static @Nullable SkillSource<BlockXpSource> findSpeedMineBreakSource(FabricAuraSkills plugin, BlockState state) {
        String path = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
        for (SkillSource<BlockXpSource> entry : plugin.getSkillManager().getSourcesOfType(BlockXpSource.class)) {
            BlockXpSource source = entry.source();
            if (!List.of(source.getTriggers()).contains(BlockXpSource.BlockTriggers.BREAK)) {
                continue;
            }
            boolean blockOk = false;
            for (String blockName : source.getBlocks()) {
                if (blockName != null && blockName.equalsIgnoreCase(path)) {
                    blockOk = true;
                    break;
                }
            }
            if (!blockOk) {
                continue;
            }
            BlockXpSource.BlockXpSourceState[] states = source.getStates();
            if (states != null && states.length > 0) {
                if (!FabricBlockStateMatcher.matchesStates(state, states)) {
                    continue;
                }
            }
            return entry;
        }
        return null;
    }

    private static void registerReadyListeners(FabricAuraSkills plugin) {
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

    private static void tryReady(FabricAuraSkills plugin, ServerPlayer player, @Nullable ServerLevel world,
            @Nullable BlockPos pos, @Nullable BlockState stateAtPos) {
        if (isDisabled()) {
            return;
        }
        if (!holdingPickaxe(player)) {
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
            plugin.getAbilityManager().sendAbilityMessage(user, plugin.getMsg(ManaAbilityMessage.SPEED_MINE_RAISE, locale));
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
                plugin.getAbilityManager().sendAbilityMessage(user, plugin.getMsg(ManaAbilityMessage.SPEED_MINE_LOWER, locale));
            }
        }, readyDurationTicks * 50L, TimeUnit.MILLISECONDS);
    }

    private static boolean isAllowReady(FabricAuraSkills plugin, ServerPlayer player, @Nullable ServerLevel world,
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
        return off.getItem() instanceof net.minecraft.world.item.BlockItem;
    }

    private static boolean tryActivate(FabricAuraSkills plugin, ServerPlayer player, User user) {
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
        int amplifier = MANA.optionInt("haste_level", 10) - 1;
        player.addEffect(new MobEffectInstance(MobEffects.HASTE, durationTicks, amplifier, false, false, true));
        if (player.level() instanceof ServerLevel soundWorld) {
            soundWorld.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 1.0f);
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
        data.setCooldown((int) MANA.getCooldown(user.getManaAbilityLevel(MANA)) * 20);
        player.removeEffect(MobEffects.HASTE);
        user.sendMessage(plugin.getMsg(ManaAbilityMessage.SPEED_MINE_END, user.getLocale()));
    }

    private static void stopIfActive(FabricAuraSkills plugin, ServerPlayer player) {
        if (!plugin.getUserManager().hasUser(player.getUUID())) {
            return;
        }
        if (plugin.getUserManager().getUser(player.getUUID()).getManaAbilityData(MANA).isActivated()) {
            stop(plugin, player);
        }
    }

    private static double consumeMana(FabricAuraSkills plugin, ServerPlayer player, User user, double manaCost) {
        double manaConsumed;
        if (plugin.configBoolean(Option.MANA_ENABLED)) {
            manaConsumed = manaCost;
            user.setMana(user.getMana() - manaConsumed);
        } else {
            manaConsumed = 0.0;
        }
        String msg = TextUtil.replace(plugin.getMsg(ManaAbilityMessage.SPEED_MINE_START, user.getLocale()),
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

    /**
     * Matches Bukkit {@code ManaAbilityProvider#failsChecks} for activation (e.g. block break).
     */
    private static boolean failsBreakActivationChecks(FabricAuraSkills plugin, ServerPlayer player, User user) {
        if (user.getManaAbilityLevel(MANA) <= 0) {
            return true;
        }
        String w = FabricUser.worldNameCompat(player);
        if (plugin.getWorldManager().isDisabledWorld(w)) {
            return true;
        }
        if (!user.hasSkillPermission(MANA.getSkill())) {
            return true;
        }
        if (plugin.configBoolean(Option.DISABLE_IN_CREATIVE_MODE) && player.getAbilities().instabuild) {
            return true;
        }
        return false;
    }

    /**
     * Player-placed block tracking (Bukkit {@code RegionManager#isPlacedBlock}) is not on Fabric yet; when
     * {@link Option#CHECK_BLOCK_REPLACE_ENABLED} is true, this hook returns false so Speed Mine is not suppressed.
     */
    private static boolean isPlayerPlacedBlock(ServerLevel world, BlockPos pos) {
        return false;
    }

    private static boolean isDisabled() {
        return !MANA.isEnabled() || !MANA.getSkill().isEnabled();
    }

    private static boolean holdingPickaxe(ServerPlayer player) {
        return player.getMainHandItem().is(ItemTags.PICKAXES);
    }
}
