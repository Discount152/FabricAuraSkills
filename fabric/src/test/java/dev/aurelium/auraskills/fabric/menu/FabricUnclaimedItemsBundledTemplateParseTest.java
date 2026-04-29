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

class FabricUnclaimedItemsBundledTemplateParseTest {

    @Test
    void bundledUnclaimedItemsYmlLoads() throws Exception {
        try (InputStream in =
                FabricUnclaimedItemsBundledTemplateParseTest.class
                        .getClassLoader()
                        .getResourceAsStream(FabricUnclaimedItemsTemplateSupport.TEMPLATE_RESOURCE_PATH)) {
            assertNotNull(in, "bundled unclaimed_items template missing from test classpath");
            String yaml = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            GraniteMenuTemplate t = GraniteMenuTemplateYamlLoader.loadString(yaml, "unclaimed_items.yml");
            assertEquals("unclaimed_items", t.id());
            assertEquals(6, t.rows());
            assertTrue(t.fill().isEmpty(), "Sprint 122: bundled unclaimed_items must not use global glass fill");

            GraniteTemplateItem claimable = t.items().get("claimable");
            assertNotNull(claimable);
            assertEquals(36, claimable.slots().size());
            assertEquals(FabricUnclaimedItemsMenuGui.builtinClaimableSlots(), claimable.slots());

            assertNav(t, "previous", GraniteTemplateActionNames.PREVIOUS_PAGE);
            assertNav(t, "next", GraniteTemplateActionNames.NEXT_PAGE);
            assertNav(t, "claim_all", GraniteTemplateActionNames.CLAIM_ALL);
            assertTrue(
                    claimable.action()
                            .filter(a -> GraniteTemplateActionNames.CLAIM_ITEM.equalsIgnoreCase(a))
                            .isPresent());

            assertEquals(48, t.items().get("previous").slots().getFirst());
            assertEquals(49, t.items().get("claim_all").slots().getFirst());
            assertEquals(50, t.items().get("next").slots().getFirst());

            assertSame(claimable, GraniteTemplateItems.findClaimableGridDefinition(t));
        }
    }

    @Test
    void placeholdersExpandForRow() {
        Map<String, String> ph = new HashMap<>();
        ph.put("item_name", "Test Item");
        ph.put("item_amount", "3");
        ph.put("page", "1");
        ph.put("total_pages", "2");
        assertEquals("Test Item", FabricUnclaimedItemsPlaceholders.expand("{item_name}", ph));
        String line = FabricUnclaimedItemsPlaceholders.expand("x {item_amount} / p {page}", ph);
        assertEquals("x 3 / p 1", line);
        assertFalse(FabricUnclaimedItemsPlaceholders.hasUnresolvedBraceSyntax(line));
    }

    @Test
    void unresolvedPlaceholderDetected() {
        Map<String, String> ph = Map.of("page", "1");
        String bad = FabricUnclaimedItemsPlaceholders.expand("{item_name}", ph);
        assertTrue(FabricUnclaimedItemsPlaceholders.hasUnresolvedBraceSyntax(bad));
    }

    private static void assertNav(GraniteMenuTemplate t, String key, String expectedAction) {
        GraniteTemplateItem item = t.items().get(key);
        assertNotNull(item, "missing items." + key);
        assertTrue(
                item.action().filter(a -> expectedAction.equalsIgnoreCase(a)).isPresent(),
                "items." + key + " action should be " + expectedAction);
    }
}
