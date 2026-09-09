package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.source.SkillSource;
import dev.aurelium.auraskills.api.source.type.BlockXpSource;
import dev.aurelium.auraskills.common.config.Option;
import dev.aurelium.auraskills.common.source.SourceTypes;
import dev.aurelium.auraskills.common.user.User;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Awards block-break XP for blocks removed by external multi-break mods (Ore Harvester, Tree
 * Harvester, vein miners).
 *
 * <p>Ore Harvester registers {@code PlayerBlockBreakEvents.BEFORE} and returns {@code false} for a
 * vein, which cancels the vanilla break outright: {@code AFTER} never fires, so
 * {@link FabricBlockBreakHandler} awarded nothing for the vein — not even for the block the player
 * actually hit. The mod then removes every ore itself via {@code Block.dropResources} followed by a
 * direct {@code Level.setBlock}, which bypasses every Fabric block event and every world-diffing
 * snapshot that runs after the break.
 *
 * <p>So we observe {@code Block.dropStacks} instead. It is invoked exactly once per harvested
 * block, carries both the real pre-break {@link BlockState} and the breaking player, and is used
 * for silk-touch harvests too (silk touch only changes the produced items, never the broken block),
 * so a silk-touched vein resolves the same Mining sources as a fortune-mined one.
 *
 * <p>Capture is a read-only {@code @Inject} at HEAD: nothing is cancelled, no world state is
 * written and no drops are altered, so the harvesting mod's behaviour is unchanged. XP is applied
 * at end of tick, after the normal break pipeline has had its chance to award the primary block, so
 * blocks already paid for by {@link FabricBlockBreakHandler} are skipped rather than double-paid.
 */
public final class FabricExternalBlockBreakXp {

    /** Ore Harvester walks at most 20 blocks from the origin; allow slack for other harvest mods. */
    private static final int MAX_DISTANCE = 32;
    private static final int MAX_BLOCKS_CEILING = 1024;
    private static final int MAX_BLOCKS_FALLBACK = 128;

    private static final ConcurrentHashMap<UUID, Window> WINDOWS = new ConcurrentHashMap<>();

    private static volatile boolean enabled;
    private static volatile int maxBlocks = MAX_BLOCKS_FALLBACK;
    /** Set while AuraSkills itself removes blocks (Treecapitator's no-XP path) so we do not award them. */
    private static boolean internalBreak;

    private FabricExternalBlockBreakXp() {
    }

    public static void register(FabricAuraSkills plugin) {
        enabled = plugin.configBoolean(Option.MOD_COMPAT_MULTI_BREAK_XP_ENABLED);
        maxBlocks = clampMaxBlocks(plugin.configInt(Option.MOD_COMPAT_MULTI_BREAK_XP_MAX_BLOCKS));
        if (!enabled) {
            plugin.logger().info("[AuraSkills] mod_compat.multi_break_xp.enabled=false — external multi-break XP off; "
                    + "vein/tree harvest mods award XP for the primary block only.");
            return;
        }
        plugin.logger().info("[AuraSkills] external multi-break XP enabled (max_blocks=" + maxBlocks
                + "); observing Block.dropStacks read-only, no block-break interference.");
        ServerTickEvents.END_SERVER_TICK.register(server -> flushAll(plugin, server.getTicks()));
    }

    static int clampMaxBlocks(int configured) {
        if (configured <= 0) {
            return MAX_BLOCKS_FALLBACK;
        }
        return Math.min(MAX_BLOCKS_CEILING, configured);
    }

    /**
     * Opens a capture window from {@code ServerPlayerInteractionManager#tryBreakBlock} HEAD. Arming
     * there rather than from {@code PlayerBlockBreakEvents.BEFORE} matters: that event short-circuits
     * on the first listener returning {@code false}, so if Ore Harvester's listener ran before ours
     * we would never see the break at all.
     */
    public static void arm(@Nullable ServerPlayerEntity player, @Nullable ServerWorld world, @Nullable BlockPos pos) {
        if (!enabled || player == null || world == null || pos == null || world.isClient()) {
            return;
        }
        WINDOWS.put(player.getUuid(), new Window(world, pos.toImmutable(), world.getServer().getTicks()));
    }

    /** Records one player-attributed block removal. Read-only: changes nothing about the break. */
    public static void capture(@Nullable BlockState state, @Nullable World world, @Nullable BlockPos pos,
            @Nullable Entity entity) {
        if (!enabled || internalBreak || state == null || pos == null || state.isAir()) {
            return;
        }
        if (!(entity instanceof ServerPlayerEntity player) || !(world instanceof ServerWorld serverWorld)) {
            return;
        }
        Window window = WINDOWS.get(player.getUuid());
        if (window == null || window.world != serverWorld || !window.origin.isWithinDistance(pos, MAX_DISTANCE)) {
            return;
        }
        window.record(pos.toImmutable(), state, maxBlocks);
    }

    /** Guards AuraSkills' own block removals from being re-awarded here (see {@link FabricTreecapitator}). */
    public static void setInternalBreak(boolean value) {
        internalBreak = value;
    }

    private static void flushAll(FabricAuraSkills plugin, long tick) {
        if (WINDOWS.isEmpty()) {
            return;
        }
        for (UUID playerId : new ArrayList<>(WINDOWS.keySet())) {
            Window window = WINDOWS.remove(playerId);
            if (window != null) {
                flush(plugin, playerId, window, tick);
            }
        }
    }

    private static void flush(FabricAuraSkills plugin, UUID playerId, Window window, long tick) {
        Map<BlockPos, BlockState> captured = window.drain();
        if (captured.isEmpty() || !plugin.isRuntimeReady()) {
            return;
        }
        ServerPlayerEntity player = window.world.getServer().getPlayerManager().getPlayer(playerId);
        if (player == null) {
            return;
        }
        int matched = 0;
        int awarded = 0;
        int dedupeSkipped = 0;
        int placedSkipped = 0;
        for (Map.Entry<BlockPos, BlockState> entry : captured.entrySet()) {
            BlockPos pos = entry.getKey();
            BlockState state = entry.getValue();
            SkillSource<BlockXpSource> match = FabricBlockXpSourceLookup.findForBreak(plugin, state);
            if (match == null) {
                continue;
            }
            matched++;
            // The normal break pipeline records every award it makes; anything already paid for this
            // break (the primary block on an uncancelled break) must not be paid twice here.
            if (FabricXpHarvestDedupe.wasAwardedSince(window.world, pos, window.tick)) {
                dedupeSkipped++;
                continue;
            }
            BlockXpSource source = match.source();
            Skill skill = match.skill();
            if (plugin.configBoolean(Option.CHECK_BLOCK_REPLACE_ENABLED) && source.checkReplace()
                    && plugin.getFabricRegionManager().isPlacedBlock(window.world, pos)) {
                placedSkipped++;
                continue;
            }
            if (FabricXpHandlerUtil.shouldBlockXp(plugin, player, skill, SourceTypes.BLOCK)) {
                continue;
            }
            User user = FabricXpUserResolution.resolve(plugin, player);
            if (user == null) {
                continue;
            }
            // No FabricBlockLevelerHelper here: the block is already air by flush time, so a live
            // world read would misreport multi-block sources. Gathering luck is skipped for the same
            // reason it must be — the harvesting mod already resolved and placed the drops.
            double xp = source.getXp() * FabricBlockStateMatcher.getStateMultiplier(state, source);
            plugin.getLevelManager().addXp(user, skill, source, xp);
            FabricXpHarvestDedupe.recordBlockBreakAward(window.world, pos, tick);
            awarded++;
        }
        boolean multiBlock = captured.size() > 1;
        if (FabricXpDiagnostics.isEnabled() && (multiBlock || awarded > 0)) {
            BlockState originState = captured.get(window.origin);
            FabricXpDiagnostics.logMultiBreakLifecycle(
                    plugin,
                    "external_capture_flush",
                    player,
                    player.isSneaking(),
                    originState == null ? "-" : Registries.BLOCK.getId(originState.getBlock()).toString(),
                    captured.size(),
                    captured.size(),
                    matched,
                    awarded,
                    dedupeSkipped,
                    placedSkipped,
                    window.overflow(),
                    reasonFromCounts(captured.size(), matched, awarded, dedupeSkipped));
        }
    }

    static String reasonFromCounts(int removedCount, int matchedCount, int awardedCount) {
        return reasonFromCounts(removedCount, matchedCount, awardedCount, 0);
    }

    static String reasonFromCounts(int removedCount, int matchedCount, int awardedCount, int dedupeSkippedCount) {
        if (removedCount <= 0) {
            return "no_removals_observed_in_window";
        }
        if (matchedCount <= 0) {
            return "removals_not_source_matched";
        }
        if (awardedCount > 0) {
            return "xp_awarded";
        }
        if (dedupeSkippedCount >= matchedCount) {
            return "already_awarded_by_break_event";
        }
        return "matched_but_blocked_or_placed";
    }

    private static final class Window {

        private final ServerWorld world;
        private final BlockPos origin;
        private final long tick;
        private final Map<BlockPos, BlockState> captured = new LinkedHashMap<>();
        private int overflow;

        Window(ServerWorld world, BlockPos origin, long tick) {
            this.world = world;
            this.origin = origin;
            this.tick = tick;
        }

        synchronized void record(BlockPos pos, BlockState state, int cap) {
            if (captured.size() >= cap) {
                overflow++;
                return;
            }
            captured.putIfAbsent(pos, state);
        }

        synchronized Map<BlockPos, BlockState> drain() {
            Map<BlockPos, BlockState> out = new LinkedHashMap<>(captured);
            captured.clear();
            return out;
        }

        synchronized int overflow() {
            return overflow;
        }
    }
}
