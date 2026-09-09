package dev.aurelium.auraskills.fabric;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Agility movement XP: polling cadence and the award arithmetic.
 *
 * <p>Whether {@code walk_one_cm} resolves against the stat registry can only be seen on a live server (Yarn's dev
 * mappings split {@code SimpleRegistry} from {@code RegistryEntry$Reference}, so JUnit cannot bootstrap the
 * registries). What is checkable here is everything downstream of resolution: the poll period, and that the
 * centimetre deltas turn into the XP amounts the manual spot-check expects.
 */
class FabricAgilityStatisticPollingTest {

    private static final double EPSILON = 1e-9;

    private static String embeddedConfig() throws IOException {
        try (InputStream in = FabricAgilityStatisticPollingTest.class.getResourceAsStream("/config.yml")) {
            assertNotNull(in, "config.yml should be on the fabric classpath (common resources merged)");
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /**
     * Upstream's 12000-tick default means movement XP lands once every 10 minutes, which reads as "Agility is
     * broken" in play. Anything above a few seconds reintroduces that.
     */
    @Test
    void statisticPollPeriodIsShortEnoughForMovementXpToBeVisible() throws IOException {
        String config = embeddedConfig();
        int i = config.indexOf("gain_period_ticks:");
        assertTrue(i >= 0, "config should define source.statistic.gain_period_ticks");
        int end = config.indexOf('\n', i);
        int ticks = Integer.parseInt(config.substring(i + "gain_period_ticks:".length(), end).trim());
        assertTrue(ticks >= 1, "poll period must be at least one tick");
        assertTrue(ticks <= 200, "poll period should stay under ~10s of ticks, was " + ticks);
    }

    /** The XP values quoted in the Agility spot-check are derived from these numbers. */
    @Test
    void agilitySourcesKeepTheThresholdsTheSpotCheckAssumes() throws IOException {
        String agility;
        try (InputStream in = FabricAgilityStatisticPollingTest.class.getResourceAsStream("/sources/agility.yml")) {
            assertNotNull(in, "agility.yml should be on the fabric classpath");
            agility = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        assertTrue(agility.contains("statistic: walk_one_cm"), "walk_per_meter should map to walk_one_cm");
        assertTrue(agility.contains("statistic: sprint_one_cm"), "sprint_per_meter should map to sprint_one_cm");
        assertTrue(agility.contains("statistic: swim_one_cm"), "swim_per_meter should map to swim_one_cm");
        assertTrue(agility.contains("minimum_increase: 1000"), "movement threshold should stay 1000 cm (10 blocks)");
        assertFalse(agility.contains("multiplier: 0.1"), "centimetre-to-metre multiplier should stay 0.01");
    }

    /** 20 blocks of travel is 2000 cm; these are the amounts the manual spot-check should observe. */
    @Test
    void twentyBlocksOfTravelAwardsTheDocumentedXp() {
        assertEquals(4.0, xpFor(2000, 0.2), EPSILON, "walking 20 blocks");
        assertEquals(1.2, xpFor(2000, 0.06), EPSILON, "sprinting 20 blocks");
        assertEquals(12.0, xpFor(2000, 0.6), EPSILON, "swimming 20 blocks");
    }

    @Test
    void travelBelowTheThresholdAwardsNothingYet() {
        FabricStatisticXpHandler.StatProgress p =
                FabricStatisticXpHandler.computeProgress(500, 0, 1000, 0.01, 0.2);
        assertFalse(p.thresholdMet(), "5 blocks is under the 10-block threshold");
        assertEquals(0.0, p.xpToAdd(), EPSILON);
        assertEquals(500, p.totalDelta(), "the partial distance must be carried, not discarded");
    }

    /**
     * The reason shortening the poll period is safe: sub-threshold deltas are banked as a remainder and paid on a
     * later poll, so a faster cadence changes only when XP lands, never how much.
     */
    @Test
    void remaindersCarryAcrossPollsSoNoDistanceIsLost() {
        FabricStatisticXpHandler.StatProgress first =
                FabricStatisticXpHandler.computeProgress(400, 0, 1000, 0.01, 0.2);
        assertFalse(first.thresholdMet());

        FabricStatisticXpHandler.StatProgress second =
                FabricStatisticXpHandler.computeProgress(400, first.totalDelta(), 1000, 0.01, 0.2);
        assertFalse(second.thresholdMet(), "800 cm is still short");

        FabricStatisticXpHandler.StatProgress third =
                FabricStatisticXpHandler.computeProgress(400, second.totalDelta(), 1000, 0.01, 0.2);
        assertTrue(third.thresholdMet(), "1200 cm crosses the threshold");
        assertEquals(1200, third.totalDelta());
        // Three small polls pay exactly what one 1200 cm poll would have.
        assertEquals(xpFor(1200, 0.2), third.xpToAdd(), EPSILON);
    }

    @Test
    void negativeOrZeroDeltasNeverAward() {
        assertEquals(0.0, FabricStatisticXpHandler.computeProgress(0, 0, 1000, 0.01, 0.2).xpToAdd(), EPSILON);
        FabricStatisticXpHandler.StatProgress negative =
                FabricStatisticXpHandler.computeProgress(-5000, 0, 1000, 0.01, 0.2);
        assertEquals(0, negative.totalDelta(), "a stat reset must not bank negative distance");
        assertEquals(0.0, negative.xpToAdd(), EPSILON);
    }

    private static double xpFor(int centimetres, double sourceXp) {
        return FabricStatisticXpHandler.computeProgress(centimetres, 0, 1000, 0.01, sourceXp).xpToAdd();
    }
}
