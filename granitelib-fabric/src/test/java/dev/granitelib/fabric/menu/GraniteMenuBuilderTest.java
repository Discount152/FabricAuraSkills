package dev.granitelib.fabric.menu;

import dev.granitelib.fabric.template.model.GraniteMenuTemplate;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GraniteMenuBuilderTest {

    @Test
    void builderLoadsTitleRowsAndSlots() {
        GraniteLoadedMenuRegistry registry = new GraniteLoadedMenuRegistry();
        GraniteMenuBuilder.menu("builder_probe")
                .title("Built Title")
                .rows(3)
                .item("marker", "paper", 4, 13)
                .loadInto(registry, unused());

        GraniteMenuTemplate loaded = registry.template("builder_probe");
        assertEquals("Built Title", loaded.title());
        assertEquals(3, loaded.rows());
        assertEquals(java.util.List.of(4, 13), loaded.items().get("marker").slots());
        assertEquals("paper", loaded.items().get("marker").material());
    }

    private static GraniteMenuListener unused() {
        return new GraniteMenuListener() {
            @Override
            public void onOpen(GraniteMenuTemplate template) {
            }

            @Override
            public void onReplaced() {
            }
        };
    }
}
