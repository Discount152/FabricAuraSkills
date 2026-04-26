package dev.aurelium.auraskills.common.ui;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Sprint 81C: one "last observed" HP/mana display tuple. The smart idle bar extends only when the
 * displayed integers <strong>change</strong> from the previous observation, not when separate
 * stable vs in-window baselines keep rediscovering the same diff every tick.
 */
public final class SmartIdleObsDisplay {

    private SmartIdleObsDisplay() {}

    /** Rounded/floored displayed values as sent in the idle action bar. */
    public record Four(int h, int maxH, int m, int maxM) {}

    /**
     * @param hasObs false on first tick for this session (after {@code resetActionBar}, join, etc.)
     * @param lastObs last committed display snapshot, or null if {@code !hasObs}
     */
    public record ObsTickResult(@NotNull Four newObs, long newVisibleUntilMs, boolean extendedWindow) {}

    public static @NotNull ObsTickResult process(
            boolean hasObs,
            @Nullable Four lastObs,
            long visibleUntilMs,
            @NotNull Four current,
            long now,
            int healthTicks,
            int manaTicks,
            long msPerTick) {
        if (!hasObs || lastObs == null) {
            return new ObsTickResult(current, visibleUntilMs, false);
        }
        boolean dHealth = current.h() != lastObs.h() || current.maxH() != lastObs.maxH();
        boolean dMana = current.m() != lastObs.m() || current.maxM() != lastObs.maxM();
        if (!dHealth && !dMana) {
            return new ObsTickResult(lastObs, visibleUntilMs, false);
        }
        int ext = pickExtendTicks(dHealth, dMana, healthTicks, manaTicks);
        long v = visibleUntilMs;
        boolean extended = ext > 0;
        if (extended) {
            v = Math.max(v, now + (long) ext * msPerTick);
        }
        return new ObsTickResult(current, v, extended);
    }

    static int pickExtendTicks(boolean dHealth, boolean dMana, int healthTicks, int manaTicks) {
        if (dHealth && dMana) {
            return Math.max(healthTicks, manaTicks);
        } else if (dHealth) {
            return healthTicks;
        } else {
            return manaTicks;
        }
    }
}
