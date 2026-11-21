package dev.granite.graniteskills.fabric;

import net.minecraft.core.BlockPos;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Drops a second award for the same player, source, dimension, event, and position inside one
 * short window. A different dimension or a different event is a separate award.
 * Both {@code LAST} and {@code OCCURRENCE} are bounded with TTL cleanup.
 */
public final class FabricXpAwardGuard {

    private static final long WINDOW_NANOS = 50_000_000L;
    /** Occurrence counters expire after this idle period so the map cannot grow for the world lifetime. */
    private static final long OCCURRENCE_TTL_NANOS = 60_000_000_000L; // 60s
    private static final int LAST_SOFT_CAP = 4096;
    private static final int OCCURRENCE_SOFT_CAP = 8192;

    private static final ConcurrentHashMap<String, Long> LAST = new ConcurrentHashMap<>();
    /** Bumps when a tracked position's block changes, so a new block is not the previous callback. */
    private static final ConcurrentHashMap<String, Occurrence> OCCURRENCE = new ConcurrentHashMap<>();

    private FabricXpAwardGuard() {
    }

    public static boolean claim(UUID playerId, String sourceId, BlockPos pos) {
        return claim(playerId, sourceId, pos, "", "block");
    }

    public static boolean claim(UUID playerId, String sourceId, BlockPos pos, String dimension, String eventKind) {
        if (playerId == null || sourceId == null) {
            return true;
        }
        long now = System.nanoTime();
        prune(now);
        String place = (dimension == null ? "" : dimension) + "|" + (pos == null ? "-" : pos.asLong());
        Occurrence occ = OCCURRENCE.compute(place, (key, existing) -> {
            if (existing == null) {
                return new Occurrence(0, now);
            }
            existing.touchedNanos = now;
            return existing;
        });
        int occurrence = occ.value;
        String key = playerId
                + "|" + sourceId
                + "|" + (dimension == null ? "" : dimension)
                + "|" + (eventKind == null ? "" : eventKind)
                + "|" + (pos == null ? "-" : pos.asLong())
                + "|" + occurrence;
        Long previous = LAST.put(key, now);
        return previous == null || now - previous > WINDOW_NANOS;
    }

    /** A later break of a new block at this position is a separate award from the callback just claimed. */
    public static void noteBlockChanged(String dimension, BlockPos pos) {
        if (dimension == null || pos == null) {
            return;
        }
        long now = System.nanoTime();
        String place = dimension + "|" + pos.asLong();
        OCCURRENCE.compute(place, (key, existing) -> {
            if (existing == null) {
                return new Occurrence(1, now);
            }
            existing.value = existing.value + 1;
            existing.touchedNanos = now;
            return existing;
        });
        prune(now);
    }

    /** Test / lifecycle hook: drop expired and over-cap entries. */
    public static void pruneNow() {
        prune(System.nanoTime());
    }

    /** Visible for tests. */
    public static int occurrenceSize() {
        return OCCURRENCE.size();
    }

    public static void clearAllForTests() {
        LAST.clear();
        OCCURRENCE.clear();
    }

    private static void prune(long now) {
        if (LAST.size() > LAST_SOFT_CAP) {
            LAST.entrySet().removeIf(entry -> now - entry.getValue() > WINDOW_NANOS);
        }
        if (OCCURRENCE.size() > OCCURRENCE_SOFT_CAP / 2) {
            Iterator<Map.Entry<String, Occurrence>> it = OCCURRENCE.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Occurrence> entry = it.next();
                if (now - entry.getValue().touchedNanos > OCCURRENCE_TTL_NANOS) {
                    it.remove();
                }
            }
        }
        if (OCCURRENCE.size() > OCCURRENCE_SOFT_CAP) {
            // Hard bound: drop oldest half by touch time.
            OCCURRENCE.entrySet().stream()
                    .sorted((a, b) -> Long.compare(a.getValue().touchedNanos, b.getValue().touchedNanos))
                    .limit(OCCURRENCE.size() - OCCURRENCE_SOFT_CAP / 2)
                    .map(Map.Entry::getKey)
                    .forEach(OCCURRENCE::remove);
        }
    }

    private static final class Occurrence {
        int value;
        long touchedNanos;

        Occurrence(int value, long touchedNanos) {
            this.value = value;
            this.touchedNanos = touchedNanos;
        }
    }
}
