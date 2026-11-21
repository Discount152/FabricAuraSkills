package dev.granitelib.fabric.menu;

import dev.granitelib.fabric.template.model.GraniteMenuTemplate;
import dev.granitelib.fabric.template.model.GraniteTemplateItem;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GraniteSharedItemsTest {

    @Test
    void oneSharedItemIsReferencedByTwoMenus() {
        GraniteSharedItems shared = new GraniteSharedItems();
        shared.define("close", "barrier", "Close", "close");
        GraniteTemplateItem skillsClose = shared.reference("close", 49);
        GraniteTemplateItem statsClose = shared.reference("close", 40);
        GraniteMenuTemplate skills = menu("skills", skillsClose);
        GraniteMenuTemplate stats = menu("stats", statsClose);

        assertEquals("barrier", skills.items().get("close").material());
        assertEquals("barrier", stats.items().get("close").material());
        assertEquals("Close", skills.items().get("close").displayName().orElseThrow());
        assertEquals("Close", stats.items().get("close").displayName().orElseThrow());
        assertEquals("close", skills.items().get("close").action().orElseThrow());
        assertEquals("close", stats.items().get("close").action().orElseThrow());
        assertEquals(49, skills.items().get("close").slots().getFirst());
        assertEquals(40, stats.items().get("close").slots().getFirst());
    }

    private static GraniteMenuTemplate menu(String id, GraniteTemplateItem close) {
        return new GraniteMenuTemplate(id, id, 6, Optional.empty(), Map.of("close", close), Optional.of("shared"));
    }
}
