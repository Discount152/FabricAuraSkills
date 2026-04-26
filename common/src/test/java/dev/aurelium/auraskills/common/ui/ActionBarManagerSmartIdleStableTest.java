package dev.aurelium.auraskills.common.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ActionBarManagerSmartIdleStableTest {

    @Test
    void extendFromStableOnlyWhenNotAlreadyInWindow() {
        assertTrue(
                ActionBarManager.shouldExtendSmartIdleWindowFromStableDiff(
                        false, true, false));
        assertTrue(
                ActionBarManager.shouldExtendSmartIdleWindowFromStableDiff(
                        false, false, true));
        assertTrue(
                ActionBarManager.shouldExtendSmartIdleWindowFromStableDiff(
                        false, true, true));
    }

    @Test
    void noExtendFromStableWhileInWindowEvenIfValuesStillDifferFromStable() {
        // 81B: after level-up, stable* stay old until the window ends; dHealth is true every tick.
        assertFalse(
                ActionBarManager.shouldExtendSmartIdleWindowFromStableDiff(
                        true, true, false));
        assertFalse(
                ActionBarManager.shouldExtendSmartIdleWindowFromStableDiff(
                        true, true, true));
    }

    @Test
    void noOpWhenNoStableDiff() {
        assertFalse(
                ActionBarManager.shouldExtendSmartIdleWindowFromStableDiff(
                        false, false, false));
        assertFalse(
                ActionBarManager.shouldExtendSmartIdleWindowFromStableDiff(
                        true, false, false));
    }
}
