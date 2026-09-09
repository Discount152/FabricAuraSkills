package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.source.SkillSource;
import dev.aurelium.auraskills.api.source.type.BlockXpSource;
import dev.aurelium.auraskills.common.config.Option;
import dev.aurelium.auraskills.common.source.SourceTypes;
import dev.aurelium.auraskills.common.user.User;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class FabricCropInteractionHarvestXp {

    private FabricCropInteractionHarvestXp() {
    }

    public static void register(FabricAuraSkills plugin) {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient() || !(player instanceof ServerPlayerEntity sp) || !(world instanceof ServerWorld sw)) {
                return ActionResult.PASS;
            }
            if (!plugin.configBoolean(Option.MOD_COMPAT_INTERACTION_HARVEST_XP_ENABLED) || !plugin.isRuntimeReady()) {
                return ActionResult.PASS;
            }
            onUse(plugin, sp, sw, hand, hitResult);
            return ActionResult.PASS;
        });
    }

    private static void onUse(FabricAuraSkills plugin, ServerPlayerEntity sp, ServerWorld sw, Hand hand,
            BlockHitResult hitResult) {
        BlockPos pos = hitResult.getBlockPos();
        FabricCropPreUseCapture.record(
                sp,
                sw,
                pos,
                sw.getBlockState(pos),
                "use_block_callback",
                hand,
                sp.getStackInHand(hand));
        FabricCropPreUseCapture.Result resolved = FabricCropPreUseCapture.resolveWithSource(sp, sw, pos);
        BlockState resolveState = resolved.state();
        String captureSource = resolved.captureSource();
        if (FabricXpDiagnostics.isEnabled()) {
            boolean rchPresent = isRightClickHarvestPresent();
            FabricXpDiagnostics.logCropCaptureAttempt(
                    plugin,
                    sp,
                    captureSource,
                    "unknown",
                    rchPresent);
        }
        if (FabricXpDiagnostics.isEnabled()) {
            FabricXpDiagnostics.logFarmingEntry(
                    plugin,
                    sp,
                    sp.isSneaking(),
                    Registries.BLOCK.getId(resolveState.getBlock()).toString(),
                    pos.getX() + "," + pos.getY() + "," + pos.getZ(),
                    "UseBlockCallback:FabricCropInteractionHarvestXp");
        }
        if (resolveState.isAir()) {
            return;
        }
        Identifier blockId = Registries.BLOCK.getId(resolveState.getBlock());
        String lookupKey = FabricFarmingCropResolver.breakLookupKey(resolveState);
        SkillSource<BlockXpSource> match = FabricFarmingCropResolver.findBreakMatch(plugin, resolveState);
        int ageBefore = readCropAge(resolveState);
        boolean configuredMature = FabricFarmingCropResolver.isMatureOrMatchingFarmBreakState(plugin, resolveState);

        if (match == null) {
            if (FabricXpDiagnostics.isEnabled() && FabricFarmingVanillaBlocks.isVanillaFarmCropBlock(blockId)) {
                if (isLikelyBrokenAgeCrop(blockId)) {
                    FabricXpDiagnostics.logXpSourceCompare(
                            plugin,
                            "farming",
                            "sugar_cane",
                            blockId.toString(),
                            configuredMature ? "source_match" : "state_timing");
                }
                FabricXpDiagnostics.logFarmingHarvestCompat(
                        plugin,
                        "interaction_harvest_probe",
                        sp,
                        blockId,
                        ageBefore,
                        readCropAge(sw.getBlockState(pos)),
                        configuredMature,
                        lookupKey,
                        false,
                        null,
                        false,
                        configuredMature ? "no_break_source_match_post_resolve" : "immature_or_non_matching_state",
                        captureSource,
                        resolveState.toString());
            }
            return;
        }
        BlockXpSource xpSource = match.source();
        if (hasInteractOrCollectTrigger(xpSource)) {
            return;
        }
        BlockXpSource.BlockXpSourceState[] states = xpSource.getStates();
        final boolean hasStateFilter = states != null && states.length > 0;
        Skill skill = match.skill();
        if (FabricXpHandlerUtil.shouldBlockXp(plugin, sp, skill, SourceTypes.BLOCK)) {
            return;
        }
        if (plugin.configBoolean(Option.CHECK_BLOCK_REPLACE_ENABLED) && xpSource.checkReplace()
                && plugin.getFabricRegionManager().isPlacedBlock(sw, pos)) {
            return;
        }
        boolean matureBefore = hasStateFilter
                ? FabricBlockStateMatcher.matchesStates(resolveState, states)
                : true;
        final BlockState preInteractionState = resolveState;
        Identifier blockIdBefore = Registries.BLOCK.getId(resolveState.getBlock());
        double mult = FabricBlockLevelerHelper.getBlocksBroken(sw, pos, xpSource, plugin)
                * FabricBlockStateMatcher.getStateMultiplier(resolveState, xpSource);
        long scheduleTick = sw.getServer().getTicks();
        UUID playerId = sp.getUuid();

        plugin.getScheduler().scheduleSync(() -> {
            ServerPlayerEntity p = sw.getServer().getPlayerManager().getPlayer(playerId);
            if (p == null) {
                return;
            }
            if (FabricXpHarvestDedupe.shouldSkipInteractionHarvestForBreakDup(sw, pos, scheduleTick)) {
                if (FabricXpDiagnostics.isEnabled()) {
                    BlockState nowD = sw.getBlockState(pos);
                    FabricXpDiagnostics.logFarmingHarvestCompat(
                            plugin,
                            "farming_interaction_harvest",
                            p,
                            blockIdBefore,
                            ageBefore,
                            readCropAge(nowD),
                            matureBefore,
                            lookupKey,
                            true,
                            xpSource.getId().toString(),
                            false,
                            "skipped_break_path_dup",
                            captureSource,
                            preInteractionState.toString());
                }
                return;
            }
            User user = FabricXpUserResolution.resolve(plugin, p);
            if (user == null) {
                return;
            }
            BlockState now = sw.getBlockState(pos);
            int ageAfter = readCropAge(now);
            Identifier blockIdNow = Registries.BLOCK.getId(now.getBlock());
            boolean stillHarvestable;
            if (hasStateFilter) {
                stillHarvestable = FabricFarmingCropResolver.findBreakMatch(plugin, now) != null
                        && FabricBlockStateMatcher.matchesStates(now, states);
            } else {
                SkillSource<BlockXpSource> nowMatch = FabricFarmingCropResolver.findBreakMatch(plugin, now);
                boolean sameConfiguredSource =
                        nowMatch != null && nowMatch.source().getId().equals(xpSource.getId());
                stillHarvestable = !now.isAir() && now.equals(preInteractionState) && sameConfiguredSource;
            }
            if (stillHarvestable && !now.isAir()) {
                if (FabricXpDiagnostics.isEnabled()) {
                    FabricXpDiagnostics.logFarmingHarvestCompat(
                            plugin,
                            "farming_interaction_harvest",
                            p,
                            blockIdBefore,
                            ageBefore,
                            ageAfter,
                            matureBefore,
                            lookupKey,
                            true,
                            xpSource.getId().toString(),
                            false,
                            "no_harvest_change_skip",
                            captureSource,
                            preInteractionState.toString());
                }
                return;
            }
            if (!blockIdNow.equals(blockIdBefore) && !now.isAir()) {
                if (FabricXpDiagnostics.isEnabled()) {
                    FabricXpDiagnostics.logFarmingHarvestCompat(
                            plugin,
                            "farming_interaction_harvest",
                            p,
                            blockIdBefore,
                            ageBefore,
                            ageAfter,
                            matureBefore,
                            lookupKey,
                            true,
                            xpSource.getId().toString(),
                            false,
                            "block_type_changed_skip",
                            captureSource,
                            preInteractionState.toString());
                }
                return;
            }
            if (hasStateFilter && !matureBefore) {
                if (FabricXpDiagnostics.isEnabled()) {
                    FabricXpDiagnostics.logFarmingHarvestCompat(
                            plugin,
                            "farming_interaction_harvest",
                            p,
                            blockIdBefore,
                            ageBefore,
                            ageAfter,
                            false,
                            lookupKey,
                            true,
                            xpSource.getId().toString(),
                            false,
                            "immature_no_award",
                            captureSource,
                            preInteractionState.toString());
                }
                return;
            }
            plugin.getLevelManager().addXp(user, skill, xpSource, xpSource.getXp() * mult);
            FabricInteractGatheringLuck.tryApply(plugin, p, user, skill, xpSource, sw, pos, now);
            if (FabricXpDiagnostics.isEnabled()) {
                FabricXpDiagnostics.logFarmingHarvestCompat(
                        plugin,
                        "farming_interaction_harvest",
                        p,
                        blockIdBefore,
                        ageBefore,
                        ageAfter,
                        matureBefore,
                        lookupKey,
                        true,
                        xpSource.getId().toString(),
                        true,
                        "awarded",
                        captureSource,
                        preInteractionState.toString());
            }
        }, 50L, TimeUnit.MILLISECONDS);
    }

    static boolean isRightClickHarvestPresent() {
        return classPresent("net.pcal.rightclickharvest.RightClickHarvest")
                || classPresent("net.pcal.rightclickharvest.config.Config");
    }

    private static boolean classPresent(String name) {
        try {
            Class.forName(name, false, FabricCropInteractionHarvestXp.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    private static boolean isLikelyBrokenAgeCrop(Identifier blockId) {
        String path = blockId.getPath();
        return "wheat".equals(path) || "carrots".equals(path) || "potatoes".equals(path);
    }

    private static boolean hasInteractOrCollectTrigger(BlockXpSource source) {
        for (BlockXpSource.BlockTriggers t : source.getTriggers()) {
            if (t == BlockXpSource.BlockTriggers.INTERACT || t == BlockXpSource.BlockTriggers.COLLECT) {
                return true;
            }
        }
        return false;
    }

    static int readCropAgeForDiagnostics(BlockState state) {
        return readCropAge(state);
    }

    private static int readCropAge(BlockState state) {
        if (state.contains(Properties.AGE_7)) {
            return state.get(Properties.AGE_7);
        }
        if (state.contains(Properties.AGE_3)) {
            return state.get(Properties.AGE_3);
        }
        if (state.contains(Properties.AGE_2)) {
            return state.get(Properties.AGE_2);
        }
        if (state.contains(Properties.AGE_1)) {
            return state.get(Properties.AGE_1);
        }
        if (state.contains(Properties.BERRIES)) {
            return state.get(Properties.BERRIES) ? 1 : 0;
        }
        if (state.contains(Properties.AGE_25)) {
            return state.get(Properties.AGE_25);
        }
        return -1;
    }
}
