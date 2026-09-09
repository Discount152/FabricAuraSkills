package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.skill.Skills;
import dev.aurelium.auraskills.fabric.FabricInteractGatheringLuck;
import dev.aurelium.auraskills.api.source.SkillSource;
import dev.aurelium.auraskills.api.source.type.BlockXpSource;
import dev.aurelium.auraskills.common.config.Option;
import dev.aurelium.auraskills.common.source.SourceTypes;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.fabric.ability.FabricMiningAbilities;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class FabricBlockBreakHandler {

    private FabricBlockBreakHandler() {
    }

    public static void register(FabricAuraSkills plugin) {
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (world.isClient() || !(player instanceof ServerPlayerEntity sp) || !(world instanceof ServerWorld sw)) {
                return true;
            }
            if (FabricXpDiagnostics.isEnabled()) {
                FabricCropPreUseCapture.record(
                        sp,
                        sw,
                        pos,
                        state,
                        "break_before_event",
                        Hand.MAIN_HAND,
                        sp.getMainHandStack());
                BlockState w = sw.getBlockState(pos);
                String posStr = pos.getX() + "," + pos.getY() + "," + pos.getZ();
                String beforeId = state == null || state.isAir()
                        ? "minecraft:air"
                        : Registries.BLOCK.getId(state.getBlock()).toString();
                String afterId = w.isAir() ? "minecraft:air" : Registries.BLOCK.getId(w.getBlock()).toString();
                FabricXpDiagnostics.logBlockBreakEntry(
                        plugin,
                        sp,
                        sp.isSneaking(),
                        beforeId,
                        afterId,
                        posStr,
                        "PlayerBlockBreakEvents.BEFORE:FabricBlockBreakHandler");
            }
            return true;
        });
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (world.isClient() || !(player instanceof ServerPlayerEntity sp) || !(world instanceof ServerWorld sw)) {
                return;
            }
            long breakTick = sw.getServer().getTicks();
            BlockState worldNow = sw.getBlockState(pos);
            String posStr = pos.getX() + "," + pos.getY() + "," + pos.getZ();
            if (FabricXpDiagnostics.isEnabled()) {
                FabricCropPreUseCapture.record(
                        sp,
                        sw,
                        pos,
                        worldNow,
                        "break_after_event",
                        Hand.MAIN_HAND,
                        sp.getMainHandStack());
                FabricXpDiagnostics.logBlockBreakEntry(
                        plugin,
                        sp,
                        sp.isSneaking(),
                        Registries.BLOCK.getId(state.getBlock()).toString(),
                        worldNow.isAir() ? "minecraft:air" : Registries.BLOCK.getId(worldNow.getBlock()).toString(),
                        posStr,
                        "PlayerBlockBreakEvents.AFTER:FabricBlockBreakHandler");
            }
            BlockState effectiveState = state;
            SkillSource<BlockXpSource> match = FabricBlockXpSourceLookup.findForBreak(plugin, effectiveState);
            if (match == null) {
                BlockState cap = FabricBlockBreakPreCapture.takeRelaxed(sp, sw, pos, breakTick);
                if (cap != null && !cap.isAir()) {
                    effectiveState = cap;
                    match = FabricBlockXpSourceLookup.findForBreak(plugin, effectiveState);
                }
            }
            if (match == null) {
                BlockState cropCap = FabricCropPreUseCapture.peek(sp, sw, pos);
                if (cropCap != null && !cropCap.isAir()) {
                    effectiveState = cropCap;
                    match = FabricBlockXpSourceLookup.findForBreak(plugin, effectiveState);
                }
            }
            if (match == null) {
                if (FabricXpDiagnostics.isEnabled()) {
                    FabricXpDiagnostics.logBlockBreakEntry(
                            plugin,
                            sp,
                            sp.isSneaking(),
                            Registries.BLOCK.getId(state.getBlock()).toString(),
                            worldNow.isAir() ? "minecraft:air" : Registries.BLOCK.getId(worldNow.getBlock()).toString(),
                            posStr,
                            "PlayerBlockBreakEvents.AFTER:no_source_match_even_with_precapture");
                    FabricXpDiagnostics.logSourceResolution(
                            plugin,
                            "block_break_resolution",
                            sp,
                            null,
                            SourceTypes.BLOCK,
                            false,
                            null,
                            0.0,
                            false,
                            "not_evaluated",
                            "pos=" + posStr + ";broken=" + Registries.BLOCK.getId(state.getBlock()),
                            FabricXpLookupNormalize.joinKeys(FabricXpLookupNormalize.normalizedKeysForBreak(state)),
                            FabricXpLookupNormalize.blockBreakCandidateCount(plugin),
                            FabricXpLookupNormalize.blockBreakCandidateSourceIdsCsv(plugin),
                            "no_break_block_source_matched");
                }
                return;
            }
            BlockXpSource xpSource = match.source();
            Skill skill = match.skill();
            if (plugin.configBoolean(Option.CHECK_BLOCK_REPLACE_ENABLED) && xpSource.checkReplace()
                    && plugin.getFabricRegionManager().isPlacedBlock(sw, pos)) {
                if (FabricXpDiagnostics.isEnabled()) {
                    FabricXpDiagnostics.logBlockCompat(
                            plugin,
                            "block_break",
                            sp,
                            skill,
                            SourceTypes.BLOCK,
                            xpSource.getId().toString(),
                            0.0,
                            false,
                            "placed_block_check_replace",
                            "vanilla_break",
                            false,
                            1,
                            0,
                            -1,
                            -1,
                            false,
                            "false");
                }
                FabricMiningAbilities mining = plugin.getAbilityManager().getAbilityImpl(FabricMiningAbilities.class);
                if (mining.dropsMineralDirectly(effectiveState)) {
                    User userL = plugin.getUserManager().getUser(sp.getUuid());
                    FabricInteractGatheringLuck.tryApply(plugin, sp, userL, skill, xpSource, sw, pos, effectiveState);
                }
                return;
            }
            if (FabricXpHandlerUtil.shouldBlockXp(plugin, sp, skill, SourceTypes.BLOCK)) {
                return;
            }
            User user = FabricXpUserResolution.resolve(plugin, sp);
            if (user == null) {
                if (FabricXpDiagnostics.isEnabled()) {
                    FabricXpDiagnostics.logBlockBreakEntry(
                            plugin,
                            sp,
                            sp.isSneaking(),
                            Registries.BLOCK.getId(effectiveState.getBlock()).toString(),
                            worldNow.isAir() ? "minecraft:air" : Registries.BLOCK.getId(worldNow.getBlock()).toString(),
                            posStr,
                            "PlayerBlockBreakEvents.AFTER:user_null_no_addxp");
                }
                return;
            }
            if (skill == Skills.EXCAVATION && FabricXpDiagnostics.isEnabled()) {
                FabricXpDiagnostics.logExcavationEntry(
                        plugin,
                        sp,
                        sp.isSneaking(),
                        Registries.BLOCK.getId(effectiveState.getBlock()).toString(),
                        posStr,
                        "PlayerBlockBreakEvents.AFTER:FabricBlockBreakHandler");
            }
            double blocksMult = FabricBlockLevelerHelper.getBlocksBroken(sw, pos, xpSource, plugin);
            double mult = blocksMult * FabricBlockStateMatcher.getStateMultiplier(effectiveState, xpSource);
            double xpAmt = xpSource.getXp() * mult;
            plugin.getLevelManager().addXp(user, skill, xpSource, xpAmt);
            FabricXpHarvestDedupe.recordBlockBreakAward(sw, pos, sw.getServer().getTicks());
            FabricInteractGatheringLuck.tryApply(plugin, sp, user, skill, xpSource, sw, pos, effectiveState);
            if (FabricXpDiagnostics.isEnabled()) {
                int age = FabricCropInteractionHarvestXp.readCropAgeForDiagnostics(effectiveState);
                FabricXpDiagnostics.logBlockCompat(
                        plugin,
                        "block_break",
                        sp,
                        skill,
                        SourceTypes.BLOCK,
                        xpSource.getId().toString(),
                        xpAmt,
                        true,
                        "awarded",
                        "vanilla_break",
                        false,
                        1,
                        1,
                        age,
                        -1,
                        true,
                        "false");
            }
        });
    }
}

final class FabricXpHarvestDedupe {

    private static final class BreakAward {
        final long tick;

        BreakAward(long tick) {
            this.tick = tick;
        }
    }

    private static final ConcurrentHashMap<String, BreakAward> BREAK_AWARD = new ConcurrentHashMap<>();

    private FabricXpHarvestDedupe() {
    }

    private static String key(ServerWorld world, BlockPos pos) {
        return FabricUser.worldNameCompat(world) + "|" + pos.asLong();
    }

    static void recordBlockBreakAward(ServerWorld world, BlockPos pos, long serverTick) {
        pruneOlderThan(serverTick - 40L);
        BREAK_AWARD.put(key(world, pos), new BreakAward(serverTick));
    }

    /**
     * Whether the normal break pipeline already awarded this position on or after {@code minTickInclusive}.
     * Used by {@link FabricExternalBlockBreakXp} so a captured primary block is not paid twice.
     */
    static boolean wasAwardedSince(ServerWorld world, BlockPos pos, long minTickInclusive) {
        BreakAward b = BREAK_AWARD.get(key(world, pos));
        return b != null && b.tick >= minTickInclusive;
    }

    static boolean shouldSkipInteractionHarvestForBreakDup(ServerWorld world, BlockPos pos, long scheduledAtTick) {
        BreakAward b = BREAK_AWARD.get(key(world, pos));
        if (b == null) {
            return false;
        }
        return b.tick >= scheduledAtTick && b.tick <= scheduledAtTick + 1L;
    }

    private static void pruneOlderThan(long minTickInclusive) {
        Iterator<Map.Entry<String, BreakAward>> it = BREAK_AWARD.entrySet().iterator();
        while (it.hasNext()) {
            if (it.next().getValue().tick < minTickInclusive) {
                it.remove();
            }
        }
    }
}
