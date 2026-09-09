package dev.aurelium.auraskills.fabric.menu;

import dev.aurelium.auraskills.api.mana.ManaAbilities;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Sprint 128: ability lore normalization + unresolved-token diagnostics.
 */
class FabricAbilityLorePipelineS128Test {

    @Test
    void normalizeExpandedDescription_stripsRemoveAndSplitsNumberedMarkers() {
        String out =
                FabricAbilityLoreNormalization.normalizeExpandedDescription(
                        "a!!REMOVE!!b<1>hint<2>more");
        assertEquals("ab\nhint\nmore", out);
    }

    @Test
    void normalizeExpandedDescription_collapsesExtraNewlines() {
        String out =
                FabricAbilityLoreNormalization.normalizeExpandedDescription("x\n\n\n\ny");
        assertEquals("x\n\ny", out);
    }

    @Test
    void findUnresolved_collectsLeaks() {
        var found = FabricAbilityLoreUnresolvedTokens.findUnresolved("{a}<2>!!REMOVE!!");
        assertTrue(found.size() >= 3, found::toString);
    }

    @Test
    void findUnresolved_emptyForCleanPlain() {
        assertFalse(
                FabricAbilityLoreUnresolvedTokens.hasAnyUnresolved(
                        "Gives Haste 10 for 12.0 seconds.\n[Right click pickaxe and mine block to activate]"));
    }

    @Test
    void representativeSpeedMineStyle_noUnresolvedAfterNormalization() {
        String afterBraces =
                "Gives Haste 10 for 12.0 seconds. <1>[Right click pickaxe and mine block to activate]";
        String plain = FabricAbilityLoreNormalization.normalizeExpandedDescription(afterBraces);
        assertFalse(FabricAbilityLoreUnresolvedTokens.hasAnyUnresolved(plain));
        assertTrue(plain.contains("\n"));
    }

    @Test
    void representativeTerraform_radiusStaticSubstitution() {
        var staticLine =
                FabricManaAbilityDescPlaceholders.applyAbilitySpecificStatic(
                        ManaAbilities.TERRAFORM,
                        "radius {radius} value {value}");
        assertEquals("radius 4 value {value}", staticLine);
    }
}