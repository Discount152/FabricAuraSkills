package dev.aurelium.auraskills.fabric;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FabricExperienceBonusTest {

    @Test
    void applyTraitToPointDelta_matchesBukkitIntCast() {
        assertEquals(0, FabricExperienceBonus.applyTraitToPointDelta(0, 50.0));
        assertEquals(15, FabricExperienceBonus.applyTraitToPointDelta(10, 50.0));
        assertEquals(-15, FabricExperienceBonus.applyTraitToPointDelta(-10, 50.0));
        assertEquals(10, FabricExperienceBonus.applyTraitToPointDelta(10, 0.0));
    }
}
