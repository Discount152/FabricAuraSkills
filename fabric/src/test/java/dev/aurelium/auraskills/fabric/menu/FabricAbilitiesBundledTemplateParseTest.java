package dev.aurelium.auraskills.fabric.menu;

import dev.granitelib.fabric.template.GraniteTemplateActionNames;
import dev.granitelib.fabric.template.GraniteTemplateItems;
import dev.granitelib.fabric.template.load.GraniteMenuTemplateYamlLoader;
import dev.granitelib.fabric.template.model.GraniteMenuTemplate;
import dev.granitelib.fabric.template.model.GraniteTemplateItem;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FabricAbilitiesBundledTemplateParseTest {

    @Test
    void bundledAbilitiesYmlLoads() throws Exception {
        try (InputStream in =
                FabricAbilitiesBundledTemplateParseTest.class
                        .getClassLoader()
                        .getResourceAsStream(FabricAbilitiesTemplateSupport.TEMPLATE_RESOURCE_PATH)) {
            assertNotNull(in, "bundled abilities template missing from test classpath");
            String yaml = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            GraniteMenuTemplate t = GraniteMenuTemplateYamlLoader.loadString(yaml, "abilities.yml");
            assertEquals("abilities", t.id());
            assertEquals(6, t.rows());
            assertTrue(t.fill().isEmpty(), "Sprint 122: bundled abilities must not use global glass fill");

            GraniteTemplateItem ability = t.items().get("ability");
            assertNotNull(ability);
            assertEquals(FabricAbilitiesMenuGui.builtinAbilityContentSlots(), ability.slots());

            GraniteTemplateItem mana = t.items().get("mana_ability");
            assertNotNull(mana);
            assertEquals(1, mana.slots().size());
            assertEquals(31, mana.slots().getFirst());

            assertNav(t, "back", GraniteTemplateActionNames.BACK);
            assertTrue(
                    mana.action()
                            .filter(a -> GraniteTemplateActionNames.MANA_ABILITY_ROW.equalsIgnoreCase(a))
                            .isPresent());

            assertSame(ability, GraniteTemplateItems.findAbilityGridDefinition(t));
            assertSame(mana, GraniteTemplateItems.findManaAbilityItemDefinition(t));

            Map<String, String> ph = new HashMap<>();
            ph.put("skill", "farming");
            ph.put("skill_display", "Farming");
            ph.put("ability", "bountiful_harvest");
            ph.put("ability_display", "Bountiful Harvest");
            ph.put("ability_unlock_level", "Unlocks at skill level III — Farming");
            ph.put("ability_status", "Locked");
            ph.put("ability_description", "More crops.");
            ph.put("mana_ability", "replenish");
            ph.put("mana_ability_display", "Replenish");
            ph.put("mana_ability_status", "Level: II");
            ph.put("mana_ability_description", "Restore hunger.");
            String title = FabricAbilitiesPlaceholders.expand("{skill_display} Abilities", ph);
            assertEquals("Farming Abilities", title);
            assertFalse(FabricAbilitiesPlaceholders.hasUnresolvedBraceSyntax(title));
        }
    }

    @Test
    void unresolvedAbilityPlaceholderDetected() {
        Map<String, String> ph = Map.of("skill", "farming");
        String bad = FabricAbilitiesPlaceholders.expand("{ability_display}", ph);
        assertTrue(FabricAbilitiesPlaceholders.hasUnresolvedBraceSyntax(bad));
    }

    private static void assertNav(GraniteMenuTemplate t, String key, String expectedAction) {
        GraniteTemplateItem item = t.items().get(key);
        assertNotNull(item, "missing items." + key);
        assertTrue(
                item.action().filter(a -> expectedAction.equalsIgnoreCase(a)).isPresent(),
                "items." + key + " action should be " + expectedAction);
    }
}
