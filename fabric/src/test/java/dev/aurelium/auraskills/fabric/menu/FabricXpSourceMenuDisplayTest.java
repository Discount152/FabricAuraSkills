package dev.aurelium.auraskills.fabric.menu;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FabricXpSourceMenuDisplayTest {

    @Test
    void prettifyIdPath_formatsUnderscoreIds() {
        assertEquals("Wheat", FabricXpSourceMenuDisplay.prettifyIdPath("minecraft:wheat"));
        assertEquals("Iron Sword", FabricXpSourceMenuDisplay.prettifyIdPath("iron_sword"));
    }

    @Test
    void hiddenSentinel_detected() {
        assertTrue(FabricXpSourceMenuDisplay.isHiddenSentinel("!!REMOVE!!"));
        assertFalse(FabricXpSourceMenuDisplay.isHiddenSentinel("Wheat"));
    }
}