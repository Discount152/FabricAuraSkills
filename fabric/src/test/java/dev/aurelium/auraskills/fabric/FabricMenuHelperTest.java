package dev.aurelium.auraskills.fabric;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FabricMenuHelperTest {

    @Test
    void getFormat_levelProgressionStatReward() {
        FabricMenuHelper h = new FabricMenuHelper();
        String f = h.getFormat("level_progression", "stat_reward_entry");
        assertTrue(f.contains("{color}"), f);
        assertTrue(f.contains("{stat}"), f);
    }

    @Test
    void getFormat_unknownFallsBackToName() {
        FabricMenuHelper h = new FabricMenuHelper();
        assertEquals("k", h.getFormat("nope", "k"));
    }
}
