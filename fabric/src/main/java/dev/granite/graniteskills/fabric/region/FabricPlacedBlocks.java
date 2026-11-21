package dev.granite.graniteskills.fabric.region;

import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Records blocks a player placed so {@code check_replace} can skip them.
 * Positions survive restart. A piston push moves the record; destroyed blocks are cleared.
 * <p>
 * Every snapshot is written by one ordered worker, including the final flush. The worker
 * copies the live set when it runs, so a newer flush cannot be overwritten by an older
 * queued snapshot. Gameplay threads only mutate the in-memory set.
 */
public final class FabricPlacedBlocks {

    private static final int WRITE_ATTEMPTS = 8;

    private static FabricPlacedBlocks instance;
    private final Path file;
    private final Set<String> positions = java.util.concurrent.ConcurrentHashMap.newKeySet();
    private final Object mutationLock = new Object();
    private final AtomicBoolean dirty = new AtomicBoolean(false);
    private final AtomicBoolean accepting = new AtomicBoolean(true);
    private final ExecutorService io = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "graniteskills-placed-blocks-io");
        t.setDaemon(true);
        return t;
    });
    private FabricGraniteSkills plugin;

    public FabricPlacedBlocks(FabricGraniteSkills plugin) {
        this(plugin.getPluginFolder().toPath().resolve("placed-blocks.txt"));
        this.plugin = plugin;
        plugin.getScheduler().timerSync(new dev.granite.graniteskills.common.scheduler.TaskRunnable() {
            @Override
            public void run() {
                scheduleFlushIfDirty(false);
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    static FabricPlacedBlocks open(Path file) {
        return new FabricPlacedBlocks(file);
    }

    private FabricPlacedBlocks(Path file) {
        this.file = file;
        instance = this;
        load();
    }

    public static boolean isPlaced(ServerLevel level, BlockPos pos) {
        if (level == null) {
            return false;
        }
        return isPlaced(level.dimension().identifier().toString(), pos);
    }

    public static boolean isPlaced(String dimensionId, BlockPos pos) {
        FabricPlacedBlocks tracker = instance;
        if (tracker == null || dimensionId == null || pos == null) {
            return false;
        }
        return tracker.positions.contains(key(dimensionId, pos));
    }

    public static void mark(ServerLevel level, BlockPos pos) {
        if (level == null) {
            return;
        }
        mark(level.dimension().identifier().toString(), pos);
    }

    public static void mark(String dimensionId, BlockPos pos) {
        FabricPlacedBlocks tracker = instance;
        if (tracker == null || dimensionId == null || pos == null) {
            return;
        }
        synchronized (tracker.mutationLock) {
            if (tracker.positions.add(key(dimensionId, pos))) {
                tracker.dirty.set(true);
            }
        }
    }

    public static void clear(ServerLevel level, BlockPos pos) {
        if (level == null) {
            return;
        }
        clear(level.dimension().identifier().toString(), pos);
    }

    public static void clear(String dimensionId, BlockPos pos) {
        FabricPlacedBlocks tracker = instance;
        if (tracker == null || dimensionId == null || pos == null) {
            return;
        }
        synchronized (tracker.mutationLock) {
            if (tracker.positions.remove(key(dimensionId, pos))) {
                tracker.dirty.set(true);
            }
        }
    }

    public static void move(ServerLevel level, BlockPos from, BlockPos to) {
        FabricPlacedBlocks tracker = instance;
        if (tracker == null || level == null || from == null || to == null) {
            return;
        }
        synchronized (tracker.mutationLock) {
            if (PlacedBlockMoves.move(tracker.positions, key(level.dimension().identifier().toString(), from),
                    key(level.dimension().identifier().toString(), to))) {
                tracker.dirty.set(true);
            }
        }
    }

    /** Flush pending writes. Blocks until this barrier and every earlier snapshot write finish. */
    public static void flush() {
        FabricPlacedBlocks tracker = instance;
        if (tracker != null) {
            tracker.scheduleFlushIfDirty(true);
        }
    }

    /**
     * Rejects further submissions, waits for queued writes plus one final snapshot, then stops the worker.
     */
    public static void shutdown() {
        FabricPlacedBlocks tracker = instance;
        if (tracker == null) {
            return;
        }
        tracker.accepting.set(false);
        tracker.awaitFinalSnapshot();
        tracker.io.shutdown();
        try {
            if (!tracker.io.awaitTermination(10, TimeUnit.SECONDS)) {
                tracker.dirty.set(true);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            tracker.dirty.set(true);
        }
    }

    private static String key(String dimensionId, BlockPos pos) {
        return dimensionId + " " + pos.getX() + " " + pos.getY() + " " + pos.getZ();
    }

    private void load() {
        if (!Files.isRegularFile(file)) {
            return;
        }
        try {
            positions.addAll(Files.readAllLines(file));
        } catch (IOException ignored) {
            // A missing or unreadable file means no remembered placements.
        }
    }

    private void scheduleFlushIfDirty(boolean await) {
        if (!accepting.get()) {
            return;
        }
        if (!await && !dirty.get()) {
            return;
        }
        Future<?> task;
        try {
            task = io.submit(this::writeLatest);
        } catch (RejectedExecutionException e) {
            dirty.set(true);
            return;
        }
        if (!await) {
            return;
        }
        awaitTask(task);
        if (dirty.get() && accepting.get()) {
            try {
                awaitTask(io.submit(this::writeLatest));
            } catch (RejectedExecutionException e) {
                dirty.set(true);
            }
        }
    }

    private void awaitFinalSnapshot() {
        try {
            awaitTask(io.submit(this::writeLatestUntilClean));
        } catch (RejectedExecutionException e) {
            dirty.set(true);
        }
        if (!dirty.get()) {
            return;
        }
        try {
            awaitTask(io.submit(this::writeLatestUntilClean));
        } catch (RejectedExecutionException e) {
            dirty.set(true);
        }
    }

    private void awaitTask(Future<?> task) {
        try {
            task.get(30, TimeUnit.SECONDS);
        } catch (TimeoutException | ExecutionException e) {
            dirty.set(true);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            dirty.set(true);
        }
    }

    /** Runs on the IO thread. Copies the live set at execution time, never a stale caller snapshot. */
    private void writeLatest() {
        List<String> snapshot;
        synchronized (mutationLock) {
            if (!dirty.get()) {
                return;
            }
            snapshot = new ArrayList<>(positions);
            dirty.set(false);
        }
        if (!writeSnapshot(snapshot)) {
            dirty.set(true);
            return;
        }
        synchronized (mutationLock) {
            if (!sameContents(snapshot, positions)) {
                dirty.set(true);
            }
        }
    }

    private void writeLatestUntilClean() {
        for (int attempt = 0; attempt < WRITE_ATTEMPTS; attempt++) {
            writeLatest();
            synchronized (mutationLock) {
                if (!dirty.get()) {
                    return;
                }
            }
        }
    }

    private static boolean sameContents(Collection<String> snapshot, Set<String> live) {
        return live.size() == snapshot.size() && live.containsAll(snapshot);
    }

    /** @return false when the file was not replaced; {@link #dirty} is set so a later flush retries */
    private boolean writeSnapshot(List<String> snapshot) {
        Path parent = file.getParent();
        Path temp = file.resolveSibling(file.getFileName().toString() + ".tmp");
        try {
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(temp, snapshot);
            try {
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        } catch (IOException e) {
            dirty.set(true);
            try {
                Files.deleteIfExists(temp);
            } catch (IOException ignored) {
            }
            if (plugin != null) {
                plugin.logger().warn("Failed to save placed-blocks.txt: " + e.getMessage());
            }
            return false;
        }
    }
}
