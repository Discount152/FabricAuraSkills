package dev.aurelium.auraskills.fabric.menu;

import dev.granitelib.fabric.template.load.GraniteMenuTemplateYamlLoader;
import dev.granitelib.fabric.template.model.GraniteMenuTemplate;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Ensures v0 YAML {@code custom_model_data} reaches {@link dev.granitelib.fabric.template.model.GraniteTemplateItem}. */
class FabricStatInfoYamlCustomModelDataParseTest {

    @Test
    void parsesCustomModelDataOnItem() {
        String yaml =
                """
                id: stat_info
                title: T
                rows: 6
                items:
                  stat:
                    material: minecraft:book
                    slot: 22
                    custom_model_data: 77
                """;
        GraniteMenuTemplate t = GraniteMenuTemplateYamlLoader.loadString(yaml, "test");
        assertTrue(t.items().get("stat").customModelData().isPresent());
        assertEquals(77, t.items().get("stat").customModelData().getAsInt());
    }
}
