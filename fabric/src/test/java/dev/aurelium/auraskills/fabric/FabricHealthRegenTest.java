package dev.aurelium.auraskills.fabric;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FabricHealthRegenTest {

    @Test
    void addBonusToAmount_matchesBukkitHealingAddition() {
        assertEquals(0.0f, FabricHealthRegen.addBonusToAmount(0.0f, 0.0));
        assertEquals(3.5f, FabricHealthRegen.addBonusToAmount(1.0f, 2.5));
    }
}
