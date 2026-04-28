package dev.aurelium.auraskills.fabric.menu;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Sprint 114 locked English stat hub blurbs — kept in sync with
 * {@link FabricMenuPresentation#STAT_HUB_SHORT_DESCRIPTIONS} (not referenced here: that class static-initializes {@code Items}).
 */
class FabricSprint114HubCopyContractTest {

    private static final Map<String, String> EXPECTED = Map.ofEntries(
            Map.entry("strength", "Increases physical damage."),
            Map.entry("crit_chance", "Increases chance to land critical hits."),
            Map.entry("crit_damage", "Increases damage dealt by critical hits."),
            Map.entry("health", "Increases your maximum HP."),
            Map.entry("regeneration", "Improves natural health recovery."),
            Map.entry("toughness", "Reduces incoming damage."),
            Map.entry("luck", "Improves chance-based rewards."),
            Map.entry("wisdom", "Improves magical and experience-based scaling."),
            Map.entry("speed", "Increases movement speed."));

    @Test
    void hubShortDescriptionsCoverLockedStatIds() {
        assertEquals(FabricStatsTemplateSupport.LOCKED_STAT_IDS.size(), EXPECTED.size());
        for (String id : FabricStatsTemplateSupport.LOCKED_STAT_IDS) {
            assertTrue(EXPECTED.containsKey(id), "missing hub blurb for " + id);
            assertTrue(!EXPECTED.get(id).isEmpty());
        }
    }
}
