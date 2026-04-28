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

class FabricLeaderboardBundledTemplateParseTest {

    @Test
    void bundledLeaderboardYmlLoads() throws Exception {
        try (InputStream in =
                FabricLeaderboardBundledTemplateParseTest.class
                        .getClassLoader()
                        .getResourceAsStream(FabricLeaderboardTemplateSupport.TEMPLATE_RESOURCE_PATH)) {
            assertNotNull(in, "bundled leaderboard template missing from test classpath");
            String yaml = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            GraniteMenuTemplate t = GraniteMenuTemplateYamlLoader.loadString(yaml, "leaderboard.yml");
            assertEquals("leaderboard", t.id());
            assertEquals(6, t.rows());
            assertTrue(t.fill().isEmpty());

            GraniteTemplateItem entry = t.items().get("entry");
            assertNotNull(entry);
            assertEquals(FabricLeaderboardMenuGui.builtinEntrySlots(), entry.slots());

            assertNav(t, "back", GraniteTemplateActionNames.BACK);
            assertNav(t, "previous", GraniteTemplateActionNames.PREVIOUS_PAGE);
            assertNav(t, "next", GraniteTemplateActionNames.NEXT_PAGE);
            assertNav(t, "refresh", GraniteTemplateActionNames.REFRESH);
            assertTrue(
                    entry.action()
                            .filter(a -> GraniteTemplateActionNames.LEADERBOARD_ROW.equalsIgnoreCase(a))
                            .isPresent());

            assertEquals(45, t.items().get("back").slots().getFirst());
            assertEquals(48, t.items().get("previous").slots().getFirst());
            assertEquals(49, t.items().get("refresh").slots().getFirst());
            assertEquals(50, t.items().get("next").slots().getFirst());

            assertSame(entry, GraniteTemplateItems.findLeaderboardEntryDefinition(t));
        }
    }

    @Test
    void placeholdersExpandForPowerRow() {
        Map<String, String> ph = new HashMap<>();
        ph.put("leaderboard_title", "Leaderboard — Power");
        ph.put("leaderboard_type", "Power");
        ph.put("skill", "");
        ph.put("skill_display", "");
        ph.put("rank", "1");
        ph.put("player", "Steve");
        ph.put("value", "42");
        ph.put("page", "1");
        ph.put("total_pages", "1");
        String title = FabricLeaderboardPlaceholders.expand("{leaderboard_title}", ph);
        assertEquals("Leaderboard — Power", title);
        String name = FabricLeaderboardPlaceholders.expand("#{rank} {player}", ph);
        assertEquals("#1 Steve", name);
        String lore = FabricLeaderboardPlaceholders.expand("Value: {value}", ph);
        assertEquals("Value: 42", lore);
        assertFalse(FabricLeaderboardPlaceholders.hasUnresolvedBraceSyntax(name));
    }

    @Test
    void averageModePlaceholders() {
        Map<String, String> ph = new HashMap<>();
        ph.put("leaderboard_type", "Skill average");
        ph.put("skill_display", "Skill average");
        assertEquals("Skill average", FabricLeaderboardPlaceholders.expand("{leaderboard_type}", ph));
    }

    @Test
    void unresolvedPlaceholderDetected() {
        Map<String, String> ph = Map.of("rank", "1");
        String bad = FabricLeaderboardPlaceholders.expand("{player}", ph);
        assertTrue(FabricLeaderboardPlaceholders.hasUnresolvedBraceSyntax(bad));
    }

    private static void assertNav(GraniteMenuTemplate t, String key, String expectedAction) {
        GraniteTemplateItem item = t.items().get(key);
        assertNotNull(item, "missing items." + key);
        assertTrue(
                item.action().filter(a -> expectedAction.equalsIgnoreCase(a)).isPresent(),
                "items." + key + " action should be " + expectedAction);
    }
}
