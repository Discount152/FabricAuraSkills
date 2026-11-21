package dev.granite.graniteskills.fabric;

import org.jetbrains.annotations.Nullable;

/**
 * Jump XP for the agility jumping source. Jumps come from the vanilla {@code minecraft:jump} statistic,
 * which the server increments for every real client jump; XP is awarded once per completed interval.
 */
public final class FabricJumpAward {

    private FabricJumpAward() {
    }

    /** Jumps recorded since the previous observation; the first observation or a lowered statistic counts none. */
    public static int newJumps(@Nullable Integer previousStat, int currentStat) {
        if (previousStat == null || currentStat <= previousStat) {
            return 0;
        }
        return currentStat - previousStat;
    }

    public static double xpForJump(int jumpsIncludingThis, int interval, double sourceXp) {
        if (interval <= 0 || jumpsIncludingThis < interval) {
            return 0.0;
        }
        return sourceXp;
    }

    /**
     * A real jump leaves the ground moving up. Step-ups stay on the ground, walking off a ledge
     * moves down, and teleports move much farther than a jump impulse.
     */
    public static boolean leftGroundUpward(boolean wasOnGround, boolean onGround, double deltaY) {
        return wasOnGround && !onGround && deltaY > 0.15 && deltaY < 1.25;
    }

    /**
     * Counts a jump from either the vanilla jump statistic or the ground-to-air movement.
     * A movement jump that the statistic has not recorded yet is remembered, so the later statistic
     * increase is not counted a second time.
     */
    public static JumpCount combine(int statJumps, boolean motionJump, int pendingStatCredit) {
        int credit = pendingStatCredit;
        if (credit > 0 && statJumps > 0) {
            int absorb = Math.min(credit, statJumps);
            statJumps -= absorb;
            credit -= absorb;
        }
        int motion = motionJump ? 1 : 0;
        int jumps = Math.max(statJumps, motion);
        if (motion > statJumps) {
            credit += motion - statJumps;
        }
        return new JumpCount(jumps, credit);
    }

    public record JumpCount(int jumps, int pendingStatCredit) {
    }
}
