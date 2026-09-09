package dev.aurelium.auraskills.fabric;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Captures {@link BlockState} at {@link net.minecraft.server.network.ServerPlayerInteractionManager#interactBlock}
 * HEAD (before {@link net.fabricmc.fabric.api.event.player.UseBlockCallback} and harvest mods mutate the world).
 * <p>
 * Right-click harvest mods can reset crops before our {@code UseBlockCallback} runs; same-tick peek restores the
 * pre-interaction state for XP source resolution.
 */
public final class FabricCropPreUseCapture {

    private static final ConcurrentHashMap<String, LinkedList<Holder>> MAP = new ConcurrentHashMap<>();
    private static final int MAX_ENTRIES_PER_KEY = 12;
    private static final AtomicLong SEQUENCE = new AtomicLong(0L);

    private FabricCropPreUseCapture() {
    }

    private static String key(UUID playerId, ServerWorld world, long posLong) {
        return playerId + "|" + world.getRegistryKey().getValue() + "|" + posLong;
    }

    public static void record(ServerPlayerEntity player, ServerWorld world, BlockPos pos, BlockState state) {
        record(player, world, pos, state, "unknown", null, null);
    }

    public static void record(
            ServerPlayerEntity player,
            ServerWorld world,
            BlockPos pos,
            BlockState state,
            String probe,
            @Nullable Hand hand,
            @Nullable ItemStack itemStack) {
        if (world.isClient()) {
            return;
        }
        long tick = world.getServer().getTicks();
        long seq = SEQUENCE.incrementAndGet();
        LinkedList<Holder> list = MAP.computeIfAbsent(key(player.getUuid(), world, pos.asLong()), ignored -> new LinkedList<>());
        list.add(new Holder(state, tick, seq, probe));
        while (list.size() > MAX_ENTRIES_PER_KEY) {
            list.removeFirst();
        }
        if (FabricXpDiagnostics.isEnabled()) {
            FabricAuraSkills plugin = FabricAuraSkills.getInstance();
            if (plugin != null) {
                String handToken = hand == null ? "-" : hand.name().toLowerCase();
                String itemToken = itemStack == null || itemStack.isEmpty()
                        ? "-"
                        : Registries.ITEM.getId(itemStack.getItem()).toString();
                FabricXpDiagnostics.logCropOrderProbe(
                        plugin,
                        probe,
                        seq,
                        player,
                        pos,
                        state.toString(),
                        readCropAge(state),
                        handToken,
                        itemToken,
                        FabricCropInteractionHarvestXp.isRightClickHarvestPresent());
            }
        }
    }

    /**
     * Same-server-tick capture only; otherwise {@code null}.
     */
    public static @Nullable BlockState peek(ServerPlayerEntity player, ServerWorld world, BlockPos pos) {
        List<Holder> holders = validHolders(player, world, pos);
        if (holders.isEmpty()) {
            return null;
        }
        return holders.get(holders.size() - 1).state;
    }

    public static BlockState resolveBlockStateForUse(ServerPlayerEntity player, ServerWorld world, BlockPos pos) {
        BlockState captured = peek(player, world, pos);
        return captured != null ? captured : world.getBlockState(pos);
    }

    /** Sprint 139: diagnostics + behavior proof for pre-harvest crop state source. */
    public static Result resolveWithSource(ServerPlayerEntity player, ServerWorld world, BlockPos pos) {
        List<Holder> holders = validHolders(player, world, pos);
        if (!holders.isEmpty()) {
            Holder mature = null;
            for (Holder h : holders) {
                if (isMatureCapture(h.state)) {
                    mature = h;
                    break;
                }
            }
            if (mature != null) {
                return new Result(mature.state, mature.probe + "_earliest_mature");
            }
            Holder latest = holders.get(holders.size() - 1);
            return new Result(latest.state, latest.probe);
        }
        return new Result(world.getBlockState(pos), "fallback_live");
    }

    public static void pruneOlderThanTick(long minTickInclusive) {
        Iterator<Map.Entry<String, LinkedList<Holder>>> it = MAP.entrySet().iterator();
        while (it.hasNext()) {
            LinkedList<Holder> holders = it.next().getValue();
            holders.removeIf(h -> h.tick < minTickInclusive);
            if (holders.isEmpty()) {
                it.remove();
            }
        }
    }

    private static final class Holder {
        final BlockState state;
        final long tick;
        final long sequence;
        final String probe;

        Holder(BlockState state, long tick, long sequence, String probe) {
            this.state = state;
            this.tick = tick;
            this.sequence = sequence;
            this.probe = probe;
        }
    }

    public record Result(BlockState state, String captureSource) {}

    private static List<Holder> validHolders(ServerPlayerEntity player, ServerWorld world, BlockPos pos) {
        LinkedList<Holder> holders = MAP.get(key(player.getUuid(), world, pos.asLong()));
        if (holders == null || holders.isEmpty()) {
            return List.of();
        }
        long nowTick = world.getServer().getTicks();
        holders.removeIf(h -> h.tick < nowTick - 2L || h.tick > nowTick);
        return holders;
    }

    private static boolean isMatureCapture(BlockState state) {
        if (state.contains(Properties.AGE_7)) {
            return state.get(Properties.AGE_7) == 7;
        }
        if (state.contains(Properties.AGE_3)) {
            return state.get(Properties.AGE_3) == 3;
        }
        if (state.contains(Properties.AGE_2)) {
            return state.get(Properties.AGE_2) == 2;
        }
        if (state.contains(Properties.AGE_1)) {
            return state.get(Properties.AGE_1) == 1;
        }
        if (state.contains(Properties.AGE_25)) {
            return state.get(Properties.AGE_25) == 25;
        }
        if (state.contains(Properties.BERRIES)) {
            return state.get(Properties.BERRIES);
        }
        return false;
    }

    static int chooseEarliestMatureAge(List<Integer> observedAges) {
        for (int age : observedAges) {
            if (age > 0) {
                return age;
            }
        }
        return observedAges.isEmpty() ? -1 : observedAges.get(observedAges.size() - 1);
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
        if (state.contains(Properties.AGE_25)) {
            return state.get(Properties.AGE_25);
        }
        if (state.contains(Properties.BERRIES)) {
            return state.get(Properties.BERRIES) ? 1 : 0;
        }
        return -1;
    }
}
