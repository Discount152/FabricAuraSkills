package dev.granite.graniteskills.fabric;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatisticXpAwardTest {

    @Test
    void statisticXpRequiresAnIncreaseAndAwardsNothingWhenItDoesNotIncrease() {
        StatisticXpAward.Result unchanged = StatisticXpAward.award(100, 100, 1000, 0.01, 0.2);
        assertEquals(0.0, unchanged.xp());
        assertFalse(unchanged.advanceBaseline());

        StatisticXpAward.Result decreased = StatisticXpAward.award(40, 100, 1000, 0.01, 0.2);
        assertEquals(0.0, decreased.xp());
        assertFalse(decreased.advanceBaseline());

        StatisticXpAward.Result belowMinimum = StatisticXpAward.award(600, 0, 1000, 0.01, 0.2);
        assertEquals(0.0, belowMinimum.xp());
        assertFalse(belowMinimum.advanceBaseline());

        StatisticXpAward.Result oneMeterBlock = StatisticXpAward.award(1000, 0, 1000, 0.01, 0.2);
        assertEquals(2.0, oneMeterBlock.xp());
        assertTrue(oneMeterBlock.advanceBaseline());

        StatisticXpAward.Result nextBlock = StatisticXpAward.award(2500, 1000, 1000, 0.01, 0.2);
        assertEquals(3.0, nextBlock.xp());
        assertTrue(nextBlock.advanceBaseline());
    }
}
