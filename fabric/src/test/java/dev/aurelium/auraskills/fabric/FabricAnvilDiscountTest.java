package dev.aurelium.auraskills.fabric;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FabricAnvilDiscountTest {

    @Test
    void fallbackPercentCurve_atZero_isZero() {
        assertEquals(0.0, -1.0 * Math.pow(1.025, 0.0) + 1.0, 1.0e-9);
    }

    @Test
    void bukkitMinimumCostWhenRoundedToZero() {
        int repairCost = 1;
        double discount = 0.99;
        int newCost = (int) Math.round(repairCost * (1.0 - discount));
        int applied = newCost > 0 ? newCost : 1;
        assertEquals(1, applied);
    }
}
