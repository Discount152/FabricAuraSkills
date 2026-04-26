package dev.aurelium.auraskills.fabric.menu;

import dev.granitelib.fabric.template.load.GraniteMenuTemplateYamlLoader;
import dev.granitelib.fabric.template.model.GraniteMenuTemplate;
import dev.granitelib.fabric.template.model.GraniteTemplateItem;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Ensures the bundled {@code sources.yml} parses and matches built-in grid / nav expectations (no Minecraft bootstrap).
 */
class FabricSourcesBundledTemplateParseTest {

    @Test
    void bundledSourcesYmlLoads() throws Exception {
        try (InputStream in =
                FabricSourcesBundledTemplateParseTest.class
                        .getClassLoader()
                        .getResourceAsStream(FabricSourcesTemplateSupport.TEMPLATE_RESOURCE_PATH)) {
            assertNotNull(in, "bundled sources template missing from test classpath");
            String yaml = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            GraniteMenuTemplate t = GraniteMenuTemplateYamlLoader.loadString(yaml, "sources.yml");
            assertEquals("sources", t.id());
            assertEquals(6, t.rows());
            assertTrue(t.fill().isPresent());

            GraniteTemplateItem source = t.items().get("source");
            assertNotNull(source);
            assertEquals(28, source.slots().size());
            assertEquals(FabricSourcesMenuGui.builtinSourceGridSlots(), source.slots());

            assertNav(t, "back", "back");
            assertNav(t, "previous", "previous_page");
            assertNav(t, "next", "next_page");
            assertNav(t, "sort", "cycle_sort");

            assertSame(source, FabricSourcesTemplateSupport.findSourceRowDef(t));

            Map<String, String> ph =
                    Map.of(
                            "skill", "farming",
                            "skill_display", "Farming",
                            "source_name", "Wheat",
                            "source_xp", "1.0",
                            "source_type", "block",
                            "sort", "Ascending",
                            "page", "1",
                            "total_pages", "1");
            String name = FabricSourcesPlaceholders.expand("{source_name}", ph);
            assertEquals("Wheat", name);
            assertFalse(FabricSourcesPlaceholders.hasUnresolvedBraceSyntax(name));
            String loreLine = FabricSourcesPlaceholders.expand("XP: {source_xp} — {skill_display}", ph);
            assertEquals("XP: 1.0 — Farming", loreLine);
            assertFalse(FabricSourcesPlaceholders.hasUnresolvedBraceSyntax(loreLine));
        }
    }

    @Test
    void unresolvedPlaceholderDetected() {
        Map<String, String> ph = Map.of("skill", "farming", "skill_display", "Farming");
        String bad = FabricSourcesPlaceholders.expand("{source_name} {not_a_supported_key}", ph);
        assertTrue(FabricSourcesPlaceholders.hasUnresolvedBraceSyntax(bad));
    }

    private static void assertNav(GraniteMenuTemplate t, String key, String expectedAction) {
        GraniteTemplateItem item = t.items().get(key);
        assertNotNull(item, "missing items." + key);
        assertTrue(
                item.action().filter(a -> expectedAction.equalsIgnoreCase(a)).isPresent(),
                "items." + key + " action should be " + expectedAction);
    }
}
