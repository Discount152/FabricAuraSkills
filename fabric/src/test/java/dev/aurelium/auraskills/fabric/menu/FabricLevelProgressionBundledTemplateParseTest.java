package dev.aurelium.auraskills.fabric.menu;

import dev.granitelib.fabric.template.GraniteTemplateActionNames;
import dev.granitelib.fabric.template.GraniteTemplateItems;
import dev.granitelib.fabric.template.load.GraniteMenuTemplateYamlLoader;
import dev.granitelib.fabric.template.model.GraniteMenuTemplate;
import dev.granitelib.fabric.template.model.GraniteTemplateItem;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FabricLevelProgressionBundledTemplateParseTest {

    @Test
    void bundledLevelProgressionYmlLoads() throws Exception {
        try (InputStream in =
                FabricLevelProgressionBundledTemplateParseTest.class
                        .getClassLoader()
                        .getResourceAsStream(FabricLevelProgressionTemplateSupport.TEMPLATE_RESOURCE_PATH)) {
            assertNotNull(in, "bundled level_progression template missing from test classpath");
            String yaml = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            GraniteMenuTemplate t = GraniteMenuTemplateYamlLoader.loadString(yaml, "level_progression.yml");
            assertEquals("level_progression", t.id());
            assertEquals(6, t.rows());
            assertTrue(t.fill().isPresent());

            GraniteTemplateItem level = t.items().get("level");
            assertNotNull(level);
            assertEquals(28, level.slots().size());
            assertEquals(FabricSourcesMenuGui.builtinSourceGridSlots(), level.slots());

            assertNav(t, "back", GraniteTemplateActionNames.BACK);
            assertNav(t, "previous", GraniteTemplateActionNames.PREVIOUS_PAGE);
            assertNav(t, "next", GraniteTemplateActionNames.NEXT_PAGE);
            assertNav(t, "sources", GraniteTemplateActionNames.OPEN_SOURCES);
            assertNav(t, "abilities", GraniteTemplateActionNames.OPEN_ABILITIES);

            assertSame(level, GraniteTemplateItems.findLevelGridDefinition(t));
            assertTrue(
                    level.action()
                            .filter(a -> GraniteTemplateActionNames.LEVEL_ROW.equalsIgnoreCase(a))
                            .isPresent());

            Map<String, String> ph =
                    Map.of(
                            "skill", "farming",
                            "skill_display", "Farming",
                            "level", "5",
                            "level_status", "Current Level",
                            "level_detail", "Progress: 12.50 / 100.00",
                            "level_xp_required", "100",
                            "current_xp", "12.5",
                            "page", "1",
                            "total_pages", "2");
            String name = FabricLevelProgressionPlaceholders.expand("Level {level}", ph);
            assertEquals("Level 5", name);
            String title =
                    FabricLevelProgressionPlaceholders.expand(
                            "<gold><bold>{skill_display}</bold></gold> <gray>Levels</gray>", ph);
            assertTrue(title.contains("Farming"), title);
            assertTrue(title.contains("Levels"), title);
            assertFalse(FabricLevelProgressionPlaceholders.hasUnresolvedBraceSyntax(name));
        }
    }

    @Test
    void unresolvedPlaceholderDetected() {
        Map<String, String> ph = Map.of("skill", "farming", "skill_display", "Farming");
        String bad = FabricLevelProgressionPlaceholders.expand("{level} {not_a_key}", ph);
        assertTrue(FabricLevelProgressionPlaceholders.hasUnresolvedBraceSyntax(bad));
    }

    private static void assertNav(GraniteMenuTemplate t, String key, String expectedAction) {
        GraniteTemplateItem item = t.items().get(key);
        assertNotNull(item, "missing items." + key);
        assertTrue(
                item.action().filter(a -> expectedAction.equalsIgnoreCase(a)).isPresent(),
                "items." + key + " action should be " + expectedAction);
    }
}
