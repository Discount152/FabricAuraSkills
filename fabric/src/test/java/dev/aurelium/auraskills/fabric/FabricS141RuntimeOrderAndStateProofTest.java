package dev.aurelium.auraskills.fabric;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FabricS141RuntimeOrderAndStateProofTest {

    @Test
    void cropProbeOrdering_prefersEarliestMatureAge() {
        assertEquals(7, FabricCropPreUseCapture.chooseEarliestMatureAge(List.of(0, 7, 0)));
        assertEquals(3, FabricCropPreUseCapture.chooseEarliestMatureAge(List.of(0, 0, 3, 0)));
    }

    @Test
    void cropAgeZeroRejectedWhenNoMatureCaptured() {
        assertEquals(0, FabricCropPreUseCapture.chooseEarliestMatureAge(List.of(0, 0, 0)));
        assertEquals(-1, FabricCropPreUseCapture.chooseEarliestMatureAge(List.of()));
    }

    @Test
    void agilityStatProgress_accumulatesUntilThresholdAndAwards() {
        FabricStatisticXpHandler.StatProgress first =
                FabricStatisticXpHandler.computeProgress(40, 0, 100, 1.0, 0.1);
        assertFalse(first.thresholdMet());
        assertEquals(40, first.totalDelta());
        assertEquals(0.0, first.xpToAdd(), 1.0E-9);

        FabricStatisticXpHandler.StatProgress second =
                FabricStatisticXpHandler.computeProgress(70, first.totalDelta(), 100, 1.0, 0.1);
        assertTrue(second.thresholdMet());
        assertEquals(110, second.totalDelta());
        assertTrue(second.xpToAdd() > 0.0);
    }

    @Test
    void multiBreakLifecycle_reasonClassificationMatchesCounts() {
        assertEquals("no_removals_observed_in_window", FabricExternalBlockBreakXp.reasonFromCounts(0, 0, 0));
        assertEquals("removals_not_source_matched", FabricExternalBlockBreakXp.reasonFromCounts(5, 0, 0));
        assertEquals("matched_but_blocked_or_placed", FabricExternalBlockBreakXp.reasonFromCounts(5, 3, 0));
        assertEquals("xp_awarded", FabricExternalBlockBreakXp.reasonFromCounts(5, 3, 2));
    }
}