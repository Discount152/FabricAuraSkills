package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.api.mana.ManaAbilities;
import dev.aurelium.auraskills.api.mana.ManaAbility;
import dev.aurelium.auraskills.api.util.NumberUtil;
import dev.aurelium.auraskills.api.source.SkillSource;
import dev.aurelium.auraskills.api.source.type.BlockXpSource;
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
import net.minecraft.block.BlockState;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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
            if (world.isClient() || !(player instanceof ServerPlayerEntity sp) || !(world instanceof ServerWorld)) {
                return true;
            }
            if (plugin.configBoolean(Option.CHECK_BLOCK_REPLACE_ENABLED) && isPlayerPlacedBlock((ServerWorld) world, pos)) {
                return true;
            }
            User user = plugin.getUserManager().getUser(sp.getUuid());
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
        String path = Registries.BLOCK.getId(state.getBlock()).getPath();
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
        if (!holdingPickaxe(player)) {
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
                plugin.getAbilityManager().sendAbilityMessage(user, plugin.getMsg(ManaAbilityMessage.SPEED_MINE_LOWER, locale));
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
        return off.getItem() instanceof net.minecraft.item.BlockItem;
    }

    private static boolean tryActivate(FabricAuraSkills plugin, ServerPlayerEntity player, User user) {
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
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, durationTicks, amplifier, false, false, true));
        if (player.getEntityWorld() instanceof ServerWorld soundWorld) {
            soundWorld.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1.0f, 1.0f);
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
        player.removeStatusEffect(StatusEffects.HASTE);
        user.sendMessage(plugin.getMsg(ManaAbilityMessage.SPEED_MINE_END, user.getLocale()));
    }

    private static void stopIfActive(FabricAuraSkills plugin, ServerPlayerEntity player) {
        if (!plugin.getUserManager().hasUser(player.getUuid())) {
            return;
        }
        if (plugin.getUserManager().getUser(player.getUuid()).getManaAbilityData(MANA).isActivated()) {
            stop(plugin, player);
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
    private static boolean failsBreakActivationChecks(FabricAuraSkills plugin, ServerPlayerEntity player, User user) {
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
        if (plugin.configBoolean(Option.DISABLE_IN_CREATIVE_MODE) && player.getAbilities().creativeMode) {
            return true;
        }
        return false;
    }

    /**
     * Player-placed block tracking (Bukkit {@code RegionManager#isPlacedBlock}) is not on Fabric yet; when
     * {@link Option#CHECK_BLOCK_REPLACE_ENABLED} is true, this hook returns false so Speed Mine is not suppressed.
     */
    private static boolean isPlayerPlacedBlock(ServerWorld world, BlockPos pos) {
        return false;
    }

    private static boolean isDisabled() {
        return !MANA.isEnabled() || !MANA.getSkill().isEnabled();
    }

    private static boolean holdingPickaxe(ServerPlayerEntity player) {
        return player.getMainHandStack().isIn(ItemTags.PICKAXES);
    }
}
