package dev.aurelium.auraskills.fabric;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FabricS138RemainingXpCategoryFixesTest {

    @Test
    void farmingAliases_matchSeedAndNamespacedForms() {
        assertTrue(FabricFarmingCropBreakAliases.matchesFarmingConfiguredBlock("wheat", "wheat_seeds"));
        assertTrue(FabricFarmingCropBreakAliases.matchesFarmingConfiguredBlock("carrots", "carrot"));
        assertTrue(FabricFarmingCropBreakAliases.matchesFarmingConfiguredBlock("potatoes", "minecraft:potato"));
        assertTrue(FabricFarmingCropBreakAliases.matchesFarmingConfiguredBlock("wheat", "minecraft:wheat"));
    }

    @Test
    void farmingAliases_preserveSugarCaneAndRejectUnrelated() {
        assertTrue(FabricFarmingCropBreakAliases.matchesFarmingConfiguredBlock("sugar_cane", "minecraft:sugar_cane"));
        assertFalse(FabricFarmingCropBreakAliases.matchesFarmingConfiguredBlock("wheat", "sugar_cane"));
    }

    @Test
    void lootMaterialMatcher_acceptsNamespacedAndPlainMaterial() {
        assertTrue(FabricLootItemMatcher.materialTokenMatchesPath("cod", "cod"));
        assertTrue(FabricLootItemMatcher.materialTokenMatchesPath("minecraft:cod", "cod"));
        assertFalse(FabricLootItemMatcher.materialTokenMatchesPath("minecraft:salmon", "cod"));
    }

}