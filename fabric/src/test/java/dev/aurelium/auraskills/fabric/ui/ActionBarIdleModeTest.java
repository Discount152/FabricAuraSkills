package dev.aurelium.auraskills.fabric.ui;

import dev.aurelium.auraskills.common.ui.ActionBarIdleMode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ActionBarIdleModeTest {

    @Test
    void cycleGoesThroughAllModes() {
        assertEquals(ActionBarIdleMode.ALWAYS, ActionBarIdleMode.SMART.nextInCycle());
        assertEquals(ActionBarIdleMode.OFF, ActionBarIdleMode.ALWAYS.nextInCycle());
        assertEquals(ActionBarIdleMode.SMART, ActionBarIdleMode.OFF.nextInCycle());
    }

    @Test
    void fromStringOrParses() {
        assertEquals(ActionBarIdleMode.SMART, ActionBarIdleMode.fromStringOr("smart", ActionBarIdleMode.ALWAYS));
        assertEquals(ActionBarIdleMode.ALWAYS, ActionBarIdleMode.fromStringOr("ALWAYS", ActionBarIdleMode.SMART));
        assertEquals(ActionBarIdleMode.OFF, ActionBarIdleMode.fromStringOr("off", ActionBarIdleMode.SMART));
    }
}
