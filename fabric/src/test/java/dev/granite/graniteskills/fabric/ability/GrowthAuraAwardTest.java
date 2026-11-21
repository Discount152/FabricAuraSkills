package dev.granite.graniteskills.fabric.ability;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GrowthAuraAwardTest {

    @Test
    void growthAuraAppliesInsideTheRadiusAndDoesNothingOutsideIt() {
        int radius = 30;
        double edge = 30.0 * 30.0;
        double outside = 31.0 * 31.0;
        assertTrue(GrowthAuraAward.inRange(edge, radius));
        assertFalse(GrowthAuraAward.inRange(outside, radius));

        int stages = GrowthAuraAward.stages(112.0, 0.5);
        assertEquals(1, stages);
        assertEquals(4, GrowthAuraAward.nextAge(3, 7, stages, true));
        assertEquals(3, GrowthAuraAward.nextAge(3, 7, stages, false));
        assertEquals(7, GrowthAuraAward.nextAge(7, 7, stages, true));
        assertEquals(0, GrowthAuraAward.stages(0.0, 0.0));
        assertEquals(3, GrowthAuraAward.nextAge(3, 7, 0, true));
    }
}
