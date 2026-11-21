package dev.granite.graniteskills.common.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ActionBarIdleModeTest {

    @Test
    void toggleHidesAnyVisibleModeAndRestoresSmart() {
        assertEquals(ActionBarIdleMode.OFF, ActionBarIdleMode.SMART.toggled());
        assertEquals(ActionBarIdleMode.OFF, ActionBarIdleMode.ALWAYS.toggled());
        assertEquals(ActionBarIdleMode.SMART, ActionBarIdleMode.OFF.toggled());
    }

    @Test
    void explicitModeBeatsLegacyBoolean() {
        ActionBarIdleMode.ActionBarLoadResolution resolution = ActionBarIdleMode.resolveOnLoad(
                true, "always", true, false, "smart");
        assertEquals(ActionBarIdleMode.ALWAYS, resolution.mode());
    }

    @Test
    void legacyTrueLoadsSmartAndLegacyFalseLoadsOff() {
        assertEquals(ActionBarIdleMode.SMART, ActionBarIdleMode.resolveOnLoad(
                false, null, true, true, "off").mode());
        assertEquals(ActionBarIdleMode.OFF, ActionBarIdleMode.resolveOnLoad(
                false, null, true, false, "smart").mode());
    }

    @Test
    void brokenModeNameFallsBackToSmart() {
        assertEquals(ActionBarIdleMode.SMART, ActionBarIdleMode.fromStringOr("nope", ActionBarIdleMode.SMART));
    }
}
