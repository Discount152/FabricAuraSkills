package dev.aurelium.auraskills.fabric;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FabricS135FarmingCropAliasTest {

    @Test
    void wheatBlockMatchesWheatSeedsConfiguredName() {
        assertTrue(FabricFarmingCropBreakAliases.matchesFarmingConfiguredBlock("wheat", "wheat_seeds"));
        assertTrue(FabricFarmingCropBreakAliases.matchesFarmingConfiguredBlock("wheat_seeds", "wheat"));
    }

    @Test
    void carrotsAndPotatoesAliases() {
        assertTrue(FabricFarmingCropBreakAliases.matchesFarmingConfiguredBlock("carrots", "carrot"));
        assertTrue(FabricFarmingCropBreakAliases.matchesFarmingConfiguredBlock("potatoes", "potato"));
    }

    @Test
    void melonStemMatchesMelon() {
        assertTrue(FabricFarmingCropBreakAliases.matchesFarmingConfiguredBlock("melon_stem", "melon"));
        assertTrue(FabricFarmingCropBreakAliases.matchesFarmingConfiguredBlock("attached_melon_stem", "melon"));
    }

    @Test
    void unrelatedBlocksDoNotAlias() {
        assertFalse(FabricFarmingCropBreakAliases.matchesFarmingConfiguredBlock("wheat", "carrots"));
        assertFalse(FabricFarmingCropBreakAliases.matchesFarmingConfiguredBlock("stone", "wheat_seeds"));
    }
}