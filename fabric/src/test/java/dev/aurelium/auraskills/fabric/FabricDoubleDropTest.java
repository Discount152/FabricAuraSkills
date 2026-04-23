package dev.aurelium.auraskills.fabric;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FabricDoubleDropTest {

    @Test
    void toChancePercent_clampsToMax() {
        assertEquals(20.0, FabricDoubleDrop.toChancePercent(50.0, 20.0), 0.0);
        assertEquals(5.0, FabricDoubleDrop.toChancePercent(5.0, 20.0), 0.0);
    }
}
