package dev.aurelium.auraskills.fabric.menu;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FabricMenuKeysTest {

    @Test
    void levelProgression_idMatchesBukkitDefaultFileName() {
        assertEquals("level_progression", FabricMenuKeys.LEVEL_PROGRESSION);
    }

    @Test
    void statInfo_idMatchesBukkitDefaultFileName() {
        assertEquals("stat_info", FabricMenuKeys.STAT_INFO);
    }
}
