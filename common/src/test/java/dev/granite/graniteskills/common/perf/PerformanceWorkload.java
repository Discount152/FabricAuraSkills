package dev.granite.graniteskills.common.perf;

/**
 * Workload and pass bar for the concurrency run. These numbers are the criteria.
 * The test reports what it measured and does not change this bar to fit a result.
 */
public final class PerformanceWorkload {

    public static final int PLAYERS = 4;
    public static final int XP_AWARDS = 250;
    public static final double XP_EACH = 1.0;
    public static final double EXPECTED_XP = XP_AWARDS * XP_EACH;
    public static final int CLAIM_STOCK = 20;
    public static final int UI_OPENS = 100;
    public static final int UI_TICKS = 100;
    public static final int SAVES_DURING_XP = 40;
    public static final long DEADLINE_MS = 5_000L;

    private PerformanceWorkload() {
    }
}
