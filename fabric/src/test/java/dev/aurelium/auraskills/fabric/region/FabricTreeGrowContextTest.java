package dev.aurelium.auraskills.fabric.region;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class FabricTreeGrowContextTest {

    @Test
    void beginEndCarriesGrowY() {
        assertNull(FabricTreeGrowContext.currentGrowY());
        FabricTreeGrowContext.begin(63);
        assertEquals(63, FabricTreeGrowContext.currentGrowY());
        FabricTreeGrowContext.end();
        assertNull(FabricTreeGrowContext.currentGrowY());
    }
}
