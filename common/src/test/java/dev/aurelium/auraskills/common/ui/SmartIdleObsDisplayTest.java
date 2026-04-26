package dev.aurelium.auraskills.common.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SmartIdleObsDisplayTest {

    private static final long MS_PER_TICK = 50L;
    private static final int HEALTH_T = 10;
    private static final int MANA_T = 10;

    @Test
    void firstObservationInitializesWithoutExtending() {
        SmartIdleObsDisplay.Four c = new SmartIdleObsDisplay.Four(20, 20, 5, 10);
        SmartIdleObsDisplay.ObsTickResult r =
                SmartIdleObsDisplay.process(false, null, 0L, c, 1000L, HEALTH_T, MANA_T, MS_PER_TICK);
        assertEquals(c, r.newObs());
        assertEquals(0L, r.newVisibleUntilMs());
        assertFalse(r.extendedWindow());
    }

    @Test
    void noExtendWhenConsecutiveTicksIdentical() {
        SmartIdleObsDisplay.Four cur = new SmartIdleObsDisplay.Four(20, 22, 5, 10);
        var r0 =
                SmartIdleObsDisplay.process(
                        false, null, 0L, cur, 1000L, HEALTH_T, MANA_T, MS_PER_TICK);
        long t0 = 10_000L;
        // Second tick: same display values -> no new extension
        var r1 =
                SmartIdleObsDisplay.process(
                        true, r0.newObs(), 10_000L, cur, t0, HEALTH_T, MANA_T, MS_PER_TICK);
        assertEquals(10_000L, r1.newVisibleUntilMs());
        assertFalse(r1.extendedWindow());
    }

    @Test
    void maxHealthChangesOnceAndExtendsVisibleUntil() {
        SmartIdleObsDisplay.Four a = new SmartIdleObsDisplay.Four(20, 20, 0, 0);
        SmartIdleObsDisplay.Four b = new SmartIdleObsDisplay.Four(20, 22, 0, 0);
        var r0 = SmartIdleObsDisplay.process(false, null, 0L, a, 0L, HEALTH_T, MANA_T, MS_PER_TICK);
        long now = 1000L;
        var r1 = SmartIdleObsDisplay.process(true, r0.newObs(), 0L, b, now, HEALTH_T, MANA_T, MS_PER_TICK);
        assertTrue(r1.extendedWindow());
        assertEquals(now + 10L * MS_PER_TICK, r1.newVisibleUntilMs());
        // Same B again — must not re-extend (81C / sticky bar)
        var r2 =
                SmartIdleObsDisplay.process(
                        true, r1.newObs(), r1.newVisibleUntilMs(), b, now + 50L, HEALTH_T, MANA_T, MS_PER_TICK);
        assertFalse(r2.extendedWindow());
        assertEquals(r1.newVisibleUntilMs(), r2.newVisibleUntilMs());
    }

    @Test
    void manaRegenOneIntegerAtATimeExtendsPerStep() {
        SmartIdleObsDisplay.Four t20 = new SmartIdleObsDisplay.Four(20, 20, 4, 10);
        SmartIdleObsDisplay.Four t21 = new SmartIdleObsDisplay.Four(20, 20, 5, 10);
        var r0 = SmartIdleObsDisplay.process(false, null, 0L, t20, 0L, HEALTH_T, MANA_T, MS_PER_TICK);
        var r1 = SmartIdleObsDisplay.process(true, r0.newObs(), 0L, t21, 100L, HEALTH_T, MANA_T, MS_PER_TICK);
        assertTrue(r1.extendedWindow());
        var r2 =
                SmartIdleObsDisplay.process(
                        true, r1.newObs(), r1.newVisibleUntilMs(), t21, 200L, HEALTH_T, MANA_T, MS_PER_TICK);
        assertFalse(r2.extendedWindow());
    }

    @Test
    void bothAxesChangeUsesMaxOfTickBudgets() {
        SmartIdleObsDisplay.Four last = new SmartIdleObsDisplay.Four(20, 20, 0, 10);
        SmartIdleObsDisplay.Four cur = new SmartIdleObsDisplay.Four(19, 20, 1, 10);
        long now = 5_000L;
        // health 20->19, mana 0->1 -> both; pick max(10,10)*50 = 500ms
        var r = SmartIdleObsDisplay.process(true, last, 0L, cur, now, 10, 8, MS_PER_TICK);
        assertTrue(r.extendedWindow());
        assertEquals(now + 10 * MS_PER_TICK, r.newVisibleUntilMs());
    }
}
