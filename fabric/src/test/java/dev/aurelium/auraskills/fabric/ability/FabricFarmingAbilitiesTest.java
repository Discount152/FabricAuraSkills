package dev.aurelium.auraskills.fabric.ability;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Pure math tests only — no {@link net.minecraft.block.Blocks} (requires MC bootstrap in unit tests).
 */
class FabricFarmingAbilitiesTest {

    @Test
    void rollExtraStagesFromAbilityValue_zeroAndOneHundred() {
        assertEquals(0, FabricFarmingAbilities.rollExtraStagesFromAbilityValue(0.0, new Random(0L)));
        assertEquals(1, FabricFarmingAbilities.rollExtraStagesFromAbilityValue(100.0, new Random(0L)));
    }
}
