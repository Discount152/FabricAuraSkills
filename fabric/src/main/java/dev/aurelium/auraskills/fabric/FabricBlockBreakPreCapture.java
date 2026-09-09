package dev.aurelium.auraskills.fabric;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Records block state at {@link PlayerBlockBreakEvents#BEFORE} so AFTER handlers can fall back when the passed
 * {@link BlockState} no longer matches mining sources (some modded multi-break paths).
 */
public final class FabricBlockBreakPreCapture {

    private static final ConcurrentHashMap<String, Holder> MAP = new ConcurrentHashMap<>();

    private FabricBlockBreakPreCapture() {
    }

    public static void register(@SuppressWarnings("unused") FabricAuraSkills plugin) {
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (world.isClient() || !(player instanceof ServerPlayerEntity sp) || !(world instanceof ServerWorld sw)) {
                return true;
            }
            if (state == null || state.isAir()) {
                return true;
            }
            long tick = sw.getServer().getTicks();
            MAP.put(breakKey(sp, sw, pos, tick), new Holder(state, tick));
            return true;
        });
    }

    public static String breakKey(ServerPlayerEntity player, ServerWorld world, BlockPos pos, long tick) {
        return player.getUuid() + "|" + FabricUser.worldNameCompat(world) + "|" + pos.asLong() + "|" + tick;
    }

    /**
     * Removes and returns the captured pre-break state for this player, position, and server tick.
     */
    public static @Nullable BlockState take(ServerPlayerEntity player, ServerWorld world, BlockPos pos, long tick) {
        Holder h = MAP.remove(breakKey(player, world, pos, tick));
        return h == null ? null : h.state;
    }

    /**
     * Same as {@link #take} but tolerates up to 5 ticks of skew between Fabric BEFORE/AFTER (Veinminer / async break).
     */
    public static @Nullable BlockState takeRelaxed(ServerPlayerEntity player, ServerWorld world, BlockPos pos,
            long afterServerTick) {
        for (long t = afterServerTick; t >= afterServerTick - 5L && t >= 0L; t--) {
            Holder h = MAP.remove(breakKey(player, world, pos, t));
            if (h != null) {
                return h.state;
            }
        }
        return null;
    }

    static void pruneOlderThanTick(long minTickInclusive) {
        Iterator<Map.Entry<String, Holder>> it = MAP.entrySet().iterator();
        while (it.hasNext()) {
            if (it.next().getValue().tick < minTickInclusive) {
                it.remove();
            }
        }
    }

    private static final class Holder {
        final BlockState state;
        final long tick;

        Holder(BlockState state, long tick) {
            this.state = state;
            this.tick = tick;
        }
    }
}
