package dev.aurelium.auraskills.fabric.command;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FabricCommandLeaderboardTextTest {

    @Test
    void globalRank1Based_page1_firstRow() {
        assertEquals(1, FabricCommandLeaderboardText.globalRank1Based(1, 0));
    }

    @Test
    void globalRank1Based_page2_firstRow() {
        assertEquals(11, FabricCommandLeaderboardText.globalRank1Based(2, 0));
    }
}
