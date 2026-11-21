package dev.granite.graniteskills.fabric;

import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.api.source.SkillSource;
import dev.granite.graniteskills.api.source.type.BlockXpSource;
import dev.granite.graniteskills.common.config.Option;
import dev.granite.graniteskills.common.source.SourceTypes;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.fabric.region.FabricPlacedBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Veinminer-style tools remove extra blocks without a player break event. Those blocks still earn their source XP.
 * The origin was already awarded by the break that started the vein, so the follow-up does not award it again.
 */
public final class FabricExternalMultiBreak {

    private static final int MAX_BLOCKS = 64;
    private static final long AWARD_MEMORY_NANOS = 2_000_000_000L;
    private static final ConcurrentHashMap<String, Long> AWARDED = new ConcurrentHashMap<>();

    private FabricExternalMultiBreak() {
    }

    public static void onAwardedBreak(FabricGraniteSkills plugin, ServerPlayer player, ServerLevel world, BlockPos origin, BlockState originState) {
        if (plugin == null || player == null || world == null || origin == null || originState == null || originState.isAir()) {
            return;
        }
        remember(player, world, origin);
        List<Snap> snaps = snapshot(world, origin, originState);
        if (snaps.size() < 2) {
            return;
        }
        SkillSource<BlockXpSource> originSource = FabricBlockBreakHandler.findBreakSource(plugin, originState);
        Skill skill = originSource == null ? null : originSource.skill();
        String initial = BuiltInRegistries.BLOCK.getKey(originState.getBlock()).toString();
        FabricXpDiagnostics.logMultiBreakEntry(plugin, player, player.isShiftKeyDown(), initial, origin.toShortString(), true, snaps.size(), "external-multi-break");
        List<Snap> frozen = List.copyOf(snaps);
        plugin.getScheduler().scheduleSync(() -> settle(plugin, player, world, skill, initial, frozen), 50L, TimeUnit.MILLISECONDS);
    }

    private static void settle(FabricGraniteSkills plugin, ServerPlayer player, ServerLevel world, Skill skill, String initial, List<Snap> snaps) {
        if (player == null) {
            return;
        }
        User user = plugin.getUserManager().getUser(player.getUUID());
        if (user == null || skill == null) {
            return;
        }
        int removed = 0;
        int matched = 0;
        int awarded = 0;
        int deduped = 0;
        int placedSkipped = 0;
        boolean checkReplace = plugin.configBoolean(Option.CHECK_BLOCK_REPLACE_ENABLED);
        for (Snap snap : snaps) {
            if (!world.hasChunkAt(snap.pos())) {
                continue;
            }
            BlockState now = world.getBlockState(snap.pos());
            if (now.getBlock() == snap.state().getBlock()) {
                continue;
            }
            removed++;
            SkillSource<BlockXpSource> match = FabricBlockBreakHandler.findBreakSource(plugin, snap.state());
            if (match == null || skill == null || !skill.equals(match.skill())) {
                continue;
            }
            matched++;
            if (alreadyAwarded(player, world, snap.pos())) {
                deduped++;
                continue;
            }
            BlockXpSource source = match.source();
            if (source.checkReplace() && checkReplace && FabricPlacedBlocks.isPlaced(world, snap.pos())) {
                placedSkipped++;
                FabricPlacedBlocks.clear(world, snap.pos());
                continue;
            }
            if (FabricXpHandlerUtil.shouldBlockXp(plugin, player, skill, SourceTypes.BLOCK)) {
                continue;
            }
            double xp = FabricBlockStateMatcher.awardedBreakXp(snap.state(), source);
            if (xp <= 0.0) {
                continue;
            }
            if (!FabricXpAwardGuard.claim(player.getUUID(), source.getId().toString(), snap.pos(), world.dimension().identifier().toString(), "break")) {
                deduped++;
                continue;
            }
            remember(player, world, snap.pos());
            plugin.getLevelManager().addXp(user, skill, source, xp);
            awarded++;
        }
        FabricXpDiagnostics.logMultiBreakWindow(plugin, player, skill, initial, snaps.size(), removed, matched, awarded, deduped, placedSkipped, "external-multi-break");
    }

    private static List<Snap> snapshot(ServerLevel world, BlockPos origin, BlockState originState) {
        Block block = originState.getBlock();
        List<Snap> snaps = new ArrayList<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> seen = new HashSet<>();
        queue.add(origin.immutable());
        seen.add(origin.immutable());
        while (!queue.isEmpty() && snaps.size() < MAX_BLOCKS) {
            BlockPos pos = queue.removeFirst();
            BlockState state = pos.equals(origin) ? originState : world.getBlockState(pos);
            if (state.getBlock() != block) {
                continue;
            }
            snaps.add(new Snap(pos.immutable(), state));
            if (snaps.size() >= MAX_BLOCKS) {
                break;
            }
            for (Direction direction : Direction.values()) {
                BlockPos next = pos.relative(direction);
                if (seen.add(next)) {
                    queue.add(next);
                }
            }
        }
        return snaps;
    }

    private static void remember(ServerPlayer player, ServerLevel world, BlockPos pos) {
        AWARDED.put(key(player, world, pos), System.nanoTime());
    }

    private static boolean alreadyAwarded(ServerPlayer player, ServerLevel world, BlockPos pos) {
        String id = key(player, world, pos);
        Long at = AWARDED.get(id);
        if (at == null) {
            return false;
        }
        if (System.nanoTime() - at > AWARD_MEMORY_NANOS) {
            AWARDED.remove(id, at);
            return false;
        }
        return true;
    }

    private static String key(ServerPlayer player, ServerLevel world, BlockPos pos) {
        return player.getUUID() + "|" + world.dimension().identifier() + "|" + pos.asLong();
    }

    private record Snap(BlockPos pos, BlockState state) {}
}
