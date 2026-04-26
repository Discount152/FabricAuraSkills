package dev.aurelium.auraskills.fabric.ui;

import dev.aurelium.auraskills.common.ui.ActionBarIdleMode;
import dev.aurelium.auraskills.common.ui.ActionBarToggleSubcommand;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    void resolveOnLoadLegacyAndExplicit() {
        ActionBarIdleMode.ActionBarLoadResolution r1 =
                ActionBarIdleMode.resolveOnLoad(
                        false, null, true, true, "always");
        assertEquals(ActionBarIdleMode.SMART, r1.mode());
        assertTrue(r1.shouldPersistWhenIdleMissing());

        ActionBarIdleMode.ActionBarLoadResolution r2 =
                ActionBarIdleMode.resolveOnLoad(
                        false, null, true, false, "smart");
        assertEquals(ActionBarIdleMode.OFF, r2.mode());
        assertTrue(r2.shouldPersistWhenIdleMissing());

        ActionBarIdleMode.ActionBarLoadResolution r3 =
                ActionBarIdleMode.resolveOnLoad(
                        true, "always", true, true, "smart");
        assertEquals(ActionBarIdleMode.ALWAYS, r3.mode());
        assertFalse(r3.shouldPersistWhenIdleMissing());

        ActionBarIdleMode.ActionBarLoadResolution r4 =
                ActionBarIdleMode.resolveOnLoad(
                        true, "smart", false, true, "always");
        assertEquals(ActionBarIdleMode.SMART, r4.mode());
        assertFalse(r4.shouldPersistWhenIdleMissing());

        ActionBarIdleMode.ActionBarLoadResolution r5 =
                ActionBarIdleMode.resolveOnLoad(
                        true, "off", false, false, "always");
        assertEquals(ActionBarIdleMode.OFF, r5.mode());
        assertFalse(r5.shouldPersistWhenIdleMissing());

        ActionBarIdleMode.ActionBarLoadResolution r6 =
                ActionBarIdleMode.resolveOnLoad(
                        false, null, false, false, "always");
        assertEquals(ActionBarIdleMode.ALWAYS, r6.mode());
        assertFalse(r6.shouldPersistWhenIdleMissing());
    }

    @Test
    void toggleSubcommandParse() {
        assertEquals(ActionBarToggleSubcommand.Kind.CYCLE, ActionBarToggleSubcommand.parse(null).kind());
        assertEquals(ActionBarToggleSubcommand.Kind.CYCLE, ActionBarToggleSubcommand.parse("").kind());
        assertEquals(
                ActionBarToggleSubcommand.Kind.STATUS,
                ActionBarToggleSubcommand.parse("Status").kind());
        var smartParsed = ActionBarToggleSubcommand.parse("smart");
        assertEquals(ActionBarToggleSubcommand.Kind.SET_MODE, smartParsed.kind());
        assertEquals(ActionBarIdleMode.SMART, smartParsed.mode());
        assertEquals(
                ActionBarToggleSubcommand.Kind.INVALID,
                ActionBarToggleSubcommand.parse("nope").kind());
    }
}
