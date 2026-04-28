package dev.granitelib.fabric.template;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraniteTemplateActionNamesTest {

    @Test
    void contractMatchesBundledYamlAuthors() {
        assertEquals("back", GraniteTemplateActionNames.BACK);
        assertEquals("stat_panel", GraniteTemplateActionNames.STAT_PANEL);
        assertEquals("source_row", GraniteTemplateActionNames.SOURCE_ROW);
        assertEquals("level_row", GraniteTemplateActionNames.LEVEL_ROW);
        assertEquals("skill_row", GraniteTemplateActionNames.SKILL_ROW);
        assertEquals("stat_row", GraniteTemplateActionNames.STAT_ROW);
        assertEquals("ability_row", GraniteTemplateActionNames.ABILITY_ROW);
        assertEquals("mana_ability_row", GraniteTemplateActionNames.MANA_ABILITY_ROW);
        assertEquals("leaderboard_row", GraniteTemplateActionNames.LEADERBOARD_ROW);
        assertEquals("refresh", GraniteTemplateActionNames.REFRESH);
        assertEquals("claim_item", GraniteTemplateActionNames.CLAIM_ITEM);
        assertEquals("claim_all", GraniteTemplateActionNames.CLAIM_ALL);
        assertEquals("open_leaderboard", GraniteTemplateActionNames.OPEN_LEADERBOARD);
        assertEquals("open_stats", GraniteTemplateActionNames.OPEN_STATS);
        assertEquals("open_unclaimed_items", GraniteTemplateActionNames.OPEN_UNCLAIMED_ITEMS);
        assertEquals("close", GraniteTemplateActionNames.CLOSE);
        assertEquals("open_sources", GraniteTemplateActionNames.OPEN_SOURCES);
        assertEquals("open_abilities", GraniteTemplateActionNames.OPEN_ABILITIES);
        assertEquals("previous_page", GraniteTemplateActionNames.PREVIOUS_PAGE);
        assertEquals("next_page", GraniteTemplateActionNames.NEXT_PAGE);
        assertEquals("cycle_sort", GraniteTemplateActionNames.CYCLE_SORT);
    }

    @Test
    void normalizeTrimsAndRootLowercase() {
        assertNull(GraniteTemplateActionNames.normalize(null));
        assertNull(GraniteTemplateActionNames.normalize("   "));
        assertEquals("back", GraniteTemplateActionNames.normalize("  BACK  "));
        assertEquals("skill_row", GraniteTemplateActionNames.normalize("Skill_Row"));
    }

    @Test
    void isKnown() {
        assertTrue(GraniteTemplateActionNames.isKnown("BACK"));
        assertTrue(GraniteTemplateActionNames.isKnown(GraniteTemplateActionNames.SOURCE_ROW));
        assertFalse(GraniteTemplateActionNames.isKnown("skill_ro"));
        assertFalse(GraniteTemplateActionNames.isKnown(null));
        assertFalse(GraniteTemplateActionNames.isKnown(""));
    }

    @Test
    void allKnownNormalizedCountsAllV0Actions() {
        assertEquals(21, GraniteTemplateActionNames.allKnownNormalized().size());
        assertTrue(GraniteTemplateActionNames.allKnownNormalized().contains(GraniteTemplateActionNames.CYCLE_SORT));
        assertTrue(GraniteTemplateActionNames.allKnownNormalized().contains(GraniteTemplateActionNames.CLOSE));
    }
}