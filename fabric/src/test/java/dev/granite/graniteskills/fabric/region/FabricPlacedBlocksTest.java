package dev.granite.graniteskills.fabric.region;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FabricPlacedBlocksTest {

    @TempDir
    Path temp;

    @Test
    void markIsPlacedAndClearRoundTripThroughTheRecordFile() {
        Path file = temp.resolve("placed-blocks.txt");
        BlockPos pos = new BlockPos(4, 64, 8);
        FabricPlacedBlocks.open(file);

        assertFalse(FabricPlacedBlocks.isPlaced("minecraft:overworld", pos));
        FabricPlacedBlocks.mark("minecraft:overworld", pos);
        assertTrue(FabricPlacedBlocks.isPlaced("minecraft:overworld", pos));
        assertFalse(FabricPlacedBlocks.isPlaced("minecraft:the_nether", pos));
        FabricPlacedBlocks.flush();

        FabricPlacedBlocks.open(file);
        assertTrue(FabricPlacedBlocks.isPlaced("minecraft:overworld", pos));

        FabricPlacedBlocks.clear("minecraft:overworld", pos);
        FabricPlacedBlocks.flush();
        assertFalse(FabricPlacedBlocks.isPlaced("minecraft:overworld", pos));

        FabricPlacedBlocks.open(file);
        assertFalse(FabricPlacedBlocks.isPlaced("minecraft:overworld", pos));
        FabricPlacedBlocks.shutdown();
    }

    @Test
    void finalFlushWaitsForOlderWriteAndKeepsNewerRecords() throws Exception {
        Path file = temp.resolve("race.txt");
        FabricPlacedBlocks tracker = FabricPlacedBlocks.open(file);
        ExecutorService io = ioExecutor(tracker);
        CountDownLatch occupied = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        io.submit(() -> {
            occupied.countDown();
            try {
                release.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        assertTrue(occupied.await(5, TimeUnit.SECONDS));

        FabricPlacedBlocks.mark("world", new BlockPos(1, 2, 3));
        Method schedule = FabricPlacedBlocks.class.getDeclaredMethod("scheduleFlushIfDirty", boolean.class);
        schedule.setAccessible(true);
        schedule.invoke(tracker, false);

        FabricPlacedBlocks.mark("world", new BlockPos(4, 5, 6));
        AtomicBoolean flushReturned = new AtomicBoolean(false);
        Thread flusher = new Thread(() -> {
            FabricPlacedBlocks.flush();
            flushReturned.set(true);
        }, "placed-blocks-flush-barrier");
        flusher.start();
        Thread.sleep(300);
        assertFalse(flushReturned.get(), "final flush returned while an older write was still held");

        release.countDown();
        flusher.join(10_000);
        assertTrue(flushReturned.get(), "final flush did not finish after the older write was released");

        String afterFlush = Files.readString(file);
        assertTrue(afterFlush.contains("world 1 2 3"), afterFlush);
        assertTrue(afterFlush.contains("world 4 5 6"), afterFlush);

        FabricPlacedBlocks.shutdown();
        assertTrue(io.awaitTermination(5, TimeUnit.SECONDS));
        String afterShutdown = Files.readString(file);
        assertTrue(afterShutdown.contains("world 1 2 3"), afterShutdown);
        assertTrue(afterShutdown.contains("world 4 5 6"), afterShutdown);

        FabricPlacedBlocks.mark("world", new BlockPos(7, 8, 9));
        FabricPlacedBlocks.flush();
        String afterRejected = Files.readString(file);
        assertFalse(afterRejected.contains("world 7 8 9"), afterRejected);

        FabricPlacedBlocks.open(file);
        assertTrue(FabricPlacedBlocks.isPlaced("world", new BlockPos(1, 2, 3)));
        assertTrue(FabricPlacedBlocks.isPlaced("world", new BlockPos(4, 5, 6)));
        assertFalse(FabricPlacedBlocks.isPlaced("world", new BlockPos(7, 8, 9)));
        FabricPlacedBlocks.shutdown();
    }

    @Test
    void failedWriteStaysDirtyUntilALaterFlushSucceeds() throws Exception {
        Path file = temp.resolve("readonly.txt");
        Files.writeString(file, "world 0 0 0\n");
        assertTrue(file.toFile().setWritable(false));
        FabricPlacedBlocks.open(file);
        BlockPos pos = new BlockPos(9, 9, 9);
        FabricPlacedBlocks.mark("world", pos);
        FabricPlacedBlocks.flush();
        assertTrue(FabricPlacedBlocks.isPlaced("world", pos));
        String blocked = Files.readString(file);
        assertFalse(blocked.contains("world 9 9 9"), blocked);

        assertTrue(file.toFile().setWritable(true));
        FabricPlacedBlocks.flush();
        String written = Files.readString(file);
        assertTrue(written.contains("world 9 9 9"), written);
        assertTrue(written.contains("world 0 0 0"), written);

        FabricPlacedBlocks.shutdown();
        FabricPlacedBlocks.open(file);
        assertTrue(FabricPlacedBlocks.isPlaced("world", pos));
        assertTrue(FabricPlacedBlocks.isPlaced("world", new BlockPos(0, 0, 0)));
        FabricPlacedBlocks.shutdown();
    }

    private static ExecutorService ioExecutor(FabricPlacedBlocks tracker) throws Exception {
        Field field = FabricPlacedBlocks.class.getDeclaredField("io");
        field.setAccessible(true);
        return (ExecutorService) field.get(tracker);
    }
}
