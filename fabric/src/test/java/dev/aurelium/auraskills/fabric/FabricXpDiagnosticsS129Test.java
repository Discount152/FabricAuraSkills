package dev.aurelium.auraskills.fabric;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FabricXpDiagnosticsS129Test {

    @Test
    void toggleFlag_roundTrips() {
        FabricXpDiagnostics.setEnabled(false);
        assertFalse(FabricXpDiagnostics.isEnabled());
        FabricXpDiagnostics.setEnabled(true);
        assertTrue(FabricXpDiagnostics.isEnabled());
        FabricXpDiagnostics.setEnabled(false);
        assertFalse(FabricXpDiagnostics.isEnabled());
    }

    @Test
    void disabledDiagnostics_doNotRequireRuntimeObjects() {
        FabricXpDiagnostics.setEnabled(false);
        FabricXpDiagnostics.logDecision(
                null,
                "test",
                null,
                null,
                null,
                null,
                0.0,
                false,
                "noop");
        assertFalse(FabricXpDiagnostics.isEnabled());
    }
}
