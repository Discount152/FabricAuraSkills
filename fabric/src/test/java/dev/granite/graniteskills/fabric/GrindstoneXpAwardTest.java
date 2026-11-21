package dev.granite.graniteskills.fabric;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GrindstoneXpAwardTest {

    @Test
    void grindstoneXpUsesTotalLevelAndAnUnmatchedActionAwardsNothing() {
        String multiplier = "{total_level}";
        assertEquals(72.0, GrindstoneXpAward.award(true, 36.0, multiplier, 2));
        assertEquals(36.0, GrindstoneXpAward.award(true, 36.0, multiplier, 1));
        assertEquals(0.0, GrindstoneXpAward.award(true, 36.0, multiplier, 0));
        assertEquals(0.0, GrindstoneXpAward.award(false, 36.0, multiplier, 5));
        assertEquals(0.0, GrindstoneXpAward.award(
                GrindstoneXpAward.isResultTake(false, true, true), 36.0, multiplier, 3));
        assertEquals(0.0, GrindstoneXpAward.award(
                GrindstoneXpAward.isResultTake(true, false, true), 36.0, multiplier, 3));
        assertEquals(0.0, GrindstoneXpAward.award(
                GrindstoneXpAward.isResultTake(true, true, false), 36.0, multiplier, 3));
        assertEquals(0.0, GrindstoneXpAward.xp(36.0, "not-a-level", 2));
    }
}
