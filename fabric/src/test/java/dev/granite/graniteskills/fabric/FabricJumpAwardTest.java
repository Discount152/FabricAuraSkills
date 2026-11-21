package dev.granite.graniteskills.fabric;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FabricJumpAwardTest {

    @Test
    void aCompletedIntervalAwardsJumpXp() {
        double total = 0.0;
        for (int count = 1; count < 100; count++) {
            double xp = FabricJumpAward.xpForJump(count, 100, 7.0);
            assertEquals(0.0, xp);
            total += xp;
        }
        total += FabricJumpAward.xpForJump(100, 100, 7.0);
        assertEquals(7.0, total);
        assertEquals(0.0, FabricJumpAward.xpForJump(100, 0, 7.0));
    }

    @Test
    void newJumpsFollowTheJumpStatistic() {
        assertEquals(0, FabricJumpAward.newJumps(null, 57));
        assertEquals(0, FabricJumpAward.newJumps(57, 57));
        assertEquals(1, FabricJumpAward.newJumps(57, 58));
        assertEquals(3, FabricJumpAward.newJumps(57, 60));
        assertEquals(0, FabricJumpAward.newJumps(57, 0));
    }

    @Test
    void leavingTheGroundMovingUpIsAJump() {
        assertTrue(FabricJumpAward.leftGroundUpward(true, false, 0.42));
        assertFalse(FabricJumpAward.leftGroundUpward(true, false, -0.4));
        assertFalse(FabricJumpAward.leftGroundUpward(true, true, 0.5));
        assertFalse(FabricJumpAward.leftGroundUpward(false, false, 0.42));
        assertFalse(FabricJumpAward.leftGroundUpward(true, false, 8.0));
    }

    @Test
    void aMotionJumpIsNotCountedAgainWhenTheStatisticCatchesUp() {
        FabricJumpAward.JumpCount first = FabricJumpAward.combine(0, true, 0);
        assertEquals(1, first.jumps());
        assertEquals(1, first.pendingStatCredit());
        FabricJumpAward.JumpCount later = FabricJumpAward.combine(1, false, first.pendingStatCredit());
        assertEquals(0, later.jumps());
        assertEquals(0, later.pendingStatCredit());
    }
}
