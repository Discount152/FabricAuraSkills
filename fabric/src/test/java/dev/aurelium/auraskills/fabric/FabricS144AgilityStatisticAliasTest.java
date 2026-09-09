package dev.aurelium.auraskills.fabric;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class FabricS144AgilityStatisticAliasTest {

    @Test
    void agilityVanillaPathForSourceKey_mapsMovementSources() {
        assertEquals("walk_one_cm", FabricStatisticSourceResolver.agilityVanillaPathForSourceKey("walk_per_meter"));
        assertEquals("sprint_one_cm", FabricStatisticSourceResolver.agilityVanillaPathForSourceKey("sprint_per_meter"));
        assertEquals("swim_one_cm", FabricStatisticSourceResolver.agilityVanillaPathForSourceKey("swim_per_meter"));
        assertNull(FabricStatisticSourceResolver.agilityVanillaPathForSourceKey("fall_damage"));
        assertNull(FabricStatisticSourceResolver.agilityVanillaPathForSourceKey(null));
    }

    @Test
    void effectiveAgilityStatisticToken_prefersAliasOverYaml() {
        assertEquals("walk_one_cm", FabricStatisticSourceResolver.effectiveAgilityStatisticToken("anything", "walk_per_meter"));
        assertEquals("walk_one_cm", FabricStatisticSourceResolver.effectiveAgilityStatisticToken("walk_one_cm", "walk_per_meter"));
    }

    @Test
    void effectiveAgilityStatisticToken_fallsBackToYamlWhenNoAlias() {
        assertEquals("custom:foo", FabricStatisticSourceResolver.effectiveAgilityStatisticToken("custom:foo", "other_source"));
    }
}