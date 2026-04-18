package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.api.item.ModifierType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Lightweight sanity check for the {@code :fabric} test source set (no Minecraft runtime).
 * Game behavior belongs in {@code :fabric:runServer} / manual client testing.
 */
class FabricModuleSanityTest {

    @Test
    void apiTypesVisibleOnFabricClasspath() {
        assertTrue(ModifierType.values().length > 0);
    }
}
