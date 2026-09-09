package dev.aurelium.auraskills.fabric;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * JVM-only proofs (no {@link net.minecraft.registry.Bootstrap}); redstone-lit variance is covered indirectly via code
 * review + sprint manual checklist.
 */
class FabricS142VeinMiningAndAgilityTest {

    @Test
    void agility_walkStatProgress_oneMeterMeetsDefaultMinimumIncrease() {
        assertTrue(FabricStatisticXpHandler.computeProgress(1000, 0, 1000, 0.01, 0.2).thresholdMet());
        assertFalse(FabricStatisticXpHandler.computeProgress(500, 0, 1000, 0.01, 0.2).thresholdMet());
    }

    @Test
    void multiBreakLitHelper_reasonStringsMatchOreHarvesterEvidence() {
        assertEquals("no_removals_observed_in_window", FabricExternalBlockBreakXp.reasonFromCounts(0, 0, 0));
        assertEquals("xp_awarded", FabricExternalBlockBreakXp.reasonFromCounts(3, 2, 2));
    }
}