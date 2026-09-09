package dev.aurelium.auraskills.fabric;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FabricS139RemainingXpMechanismFixesTest {

    @Test
    void farmingAliasStillMatchesMatureCropFamilies() {
        assertTrue(FabricFarmingCropBreakAliases.matchesFarmingConfiguredBlock("wheat", "minecraft:wheat"));
        assertTrue(FabricFarmingCropBreakAliases.matchesFarmingConfiguredBlock("carrots", "minecraft:carrot"));
        assertTrue(FabricFarmingCropBreakAliases.matchesFarmingConfiguredBlock("potatoes", "minecraft:potato"));
        assertFalse(FabricFarmingCropBreakAliases.matchesFarmingConfiguredBlock("wheat", "minecraft:carrot"));
    }

    @Test
    void agilityStatisticTokenNormalization_handlesNamespacedAndPlain() {
        assertEquals("walk_one_cm", FabricStatisticSourceResolver.normalizeStatisticToken("walk_one_cm"));
        assertEquals("walk_one_cm", FabricStatisticSourceResolver.normalizeStatisticToken("minecraft:walk_one_cm"));
        assertEquals("sprint_one_cm", FabricStatisticSourceResolver.normalizeStatisticToken("minecraft:sprint_one_cm"));
        assertEquals("swim_one_cm", FabricStatisticSourceResolver.normalizeStatisticToken("swim_one_cm"));
    }

    @Test
    void fishingMaterialMatcherStillAcceptsNamespacedConfig() {
        assertTrue(FabricLootItemMatcher.materialTokenMatchesPath("minecraft:cod", "cod"));
        assertTrue(FabricLootItemMatcher.materialTokenMatchesPath("cod", "cod"));
        assertFalse(FabricLootItemMatcher.materialTokenMatchesPath("minecraft:salmon", "cod"));
    }
}