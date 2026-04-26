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

class FabricSkillsStatsBundledTemplateParseTest {

    @Test
    void bundledSkillsYmlLoads() throws Exception {
        try (InputStream in =
                FabricSkillsStatsBundledTemplateParseTest.class
                        .getClassLoader()
                        .getResourceAsStream(FabricSkillsTemplateSupport.TEMPLATE_RESOURCE_PATH)) {
            assertNotNull(in, "bundled skills template missing from test classpath");
            String yaml = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            GraniteMenuTemplate t = GraniteMenuTemplateYamlLoader.loadString(yaml, "skills.yml");
            assertEquals("skills", t.id());
            assertEquals(6, t.rows());
            assertTrue(t.fill().isPresent());

            GraniteTemplateItem skill = t.items().get("skill");
            assertNotNull(skill);
            assertEquals(28, skill.slots().size());
            assertEquals(FabricSourcesMenuGui.builtinSourceGridSlots(), skill.slots());

            assertNav(t, "previous", GraniteTemplateActionNames.PREVIOUS_PAGE);
            assertNav(t, "next", GraniteTemplateActionNames.NEXT_PAGE);

            assertSame(skill, GraniteTemplateItems.findSkillGridDefinition(t));
            assertTrue(
                    skill.action()
                            .filter(a -> GraniteTemplateActionNames.SKILL_ROW.equalsIgnoreCase(a))
                            .isPresent());

            Map<String, String> ph =
                    Map.of(
                            "skill", "farming",
                            "skill_display", "Farming",
                            "skill_level", "12",
                            "skill_xp", "3.50",
                            "skill_xp_in_level", "3.50",
                            "page", "1",
                            "total_pages", "1");
            String name = FabricSkillsPlaceholders.expand("{skill_display} ({skill})", ph);
            assertEquals("Farming (farming)", name);
            assertFalse(FabricSkillsPlaceholders.hasUnresolvedBraceSyntax(name));
        }
    }

    @Test
    void bundledStatsYmlLoads() throws Exception {
        try (InputStream in =
                FabricSkillsStatsBundledTemplateParseTest.class
                        .getClassLoader()
                        .getResourceAsStream(FabricStatsTemplateSupport.TEMPLATE_RESOURCE_PATH)) {
            assertNotNull(in, "bundled stats template missing from test classpath");
            String yaml = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            GraniteMenuTemplate t = GraniteMenuTemplateYamlLoader.loadString(yaml, "stats.yml");
            assertEquals("stats", t.id());
            assertEquals(6, t.rows());

            GraniteTemplateItem stat = t.items().get("stat");
            assertNotNull(stat);
            assertEquals(28, stat.slots().size());
            assertEquals(FabricSourcesMenuGui.builtinSourceGridSlots(), stat.slots());

            assertNav(t, "previous", GraniteTemplateActionNames.PREVIOUS_PAGE);
            assertNav(t, "next", GraniteTemplateActionNames.NEXT_PAGE);

            assertSame(stat, GraniteTemplateItems.findStatGridDefinition(t));

            Map<String, String> ph =
                    Map.of(
                            "stat", "health",
                            "stat_display", "Health",
                            "stat_value", "25.0",
                            "stat_description", "Max HP bonus.",
                            "page", "1",
                            "total_pages", "2");
            String line = FabricStatsPlaceholders.expand("Value: {stat_value}", ph);
            assertEquals("Value: 25.0", line);
            assertFalse(FabricStatsPlaceholders.hasUnresolvedBraceSyntax(line));
        }
    }

    @Test
    void unresolvedSkillPlaceholderDetected() {
        Map<String, String> ph = Map.of("skill", "farming");
        String bad = FabricSkillsPlaceholders.expand("{skill_display}", ph);
        assertTrue(FabricSkillsPlaceholders.hasUnresolvedBraceSyntax(bad));
    }

    @Test
    void unresolvedStatPlaceholderDetected() {
        Map<String, String> ph = Map.of("stat", "health");
        String bad = FabricStatsPlaceholders.expand("{stat_value}", ph);
        assertTrue(FabricStatsPlaceholders.hasUnresolvedBraceSyntax(bad));
    }

    private static void assertNav(GraniteMenuTemplate t, String key, String expectedAction) {
        GraniteTemplateItem item = t.items().get(key);
        assertNotNull(item, "missing items." + key);
        assertTrue(
                item.action().filter(a -> expectedAction.equalsIgnoreCase(a)).isPresent(),
                "items." + key + " action should be " + expectedAction);
    }
}
