package dev.granitelib.fabric.template.load;

import dev.granitelib.fabric.menu.GraniteLoadedMenuRegistry;
import dev.granitelib.fabric.menu.GraniteMenuListener;
import dev.granitelib.fabric.template.model.GraniteMenuTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraniteMenuExporterTest {

    @TempDir
    Path temp;

    @Test
    void exportWritesTheLoadedMenuRatherThanThePreviousFile() throws Exception {
        Path source = temp.resolve("menu.yml");
        Path exported = temp.resolve("out").resolve("menu.yml");
        Files.writeString(source, """
                id: export_probe
                title: First
                rows: 2
                fill:
                  material: gray_stained_glass_pane
                  name: Fill
                items:
                  marker:
                    material: paper
                    slot: 4
                    name: Keep
                    lore:
                      - Default lore
                    action: open
                """);
        GraniteLoadedMenuRegistry registry = new GraniteLoadedMenuRegistry();
        registry.load(GraniteMenuTemplateYamlLoader.loadPath(source), unused());
        Files.writeString(source, """
                id: export_probe
                title: Second
                rows: 2
                fill:
                  material: gray_stained_glass_pane
                  name: Fill
                items:
                  marker:
                    material: paper
                    slot: 4
                    name: Changed
                    lore:
                      - Default lore
                    action: open
                """);
        registry.reload("export_probe", source, unused());
        registry.export("export_probe", exported);

        String text = Files.readString(exported);
        GraniteMenuTemplate written = GraniteMenuTemplateYamlLoader.loadPath(exported);
        assertFalse(text.contains("First"));
        assertEquals("Second", written.title());
        assertEquals("Changed", written.items().get("marker").displayName().orElseThrow());
        assertEquals("paper", written.items().get("marker").material());
        assertEquals(4, written.items().get("marker").slots().getFirst());
        assertEquals("Default lore", written.items().get("marker").lore().getFirst());
        assertEquals("open", written.items().get("marker").action().orElseThrow());
        assertEquals("gray_stained_glass_pane", written.fill().orElseThrow().material());
        assertTrue(text.contains("title: Second"));
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
