package dev.granitelib.fabric.template.load;

import dev.granitelib.fabric.template.model.GraniteMenuTemplate;
import dev.granitelib.fabric.template.model.GraniteTemplateItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraniteMenuOverlayTest {

    @TempDir
    Path temp;

    @Test
    void overlayReplacesSpecifiedValuesAndKeepsTheRest() throws Exception {
        Path defaults = temp.resolve("default.yml");
        Path overlay = temp.resolve("user.yml");
        Files.writeString(defaults, """
                id: skills
                title: Default Title
                rows: 3
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
                  other:
                    material: book
                    slot: 0
                    name: Other
                """);
        Files.writeString(overlay, """
                title: Overlay Title
                items:
                  marker:
                    name: Changed
                """);

        GraniteMenuTemplate menu = GraniteMenuOverlay.applyFiles(defaults, overlay);
        GraniteTemplateItem marker = menu.items().get("marker");
        GraniteTemplateItem other = menu.items().get("other");

        assertEquals("skills", menu.id());
        assertEquals("Overlay Title", menu.title());
        assertEquals(3, menu.rows());
        assertEquals("gray_stained_glass_pane", menu.fill().orElseThrow().material());
        assertEquals("Fill", menu.fill().orElseThrow().displayName().orElseThrow());
        assertEquals("paper", marker.material());
        assertEquals(4, marker.slots().getFirst());
        assertEquals("Changed", marker.displayName().orElseThrow());
        assertEquals(List.of("Default lore"), marker.lore());
        assertEquals("open", marker.action().orElseThrow());
        assertEquals("book", other.material());
        assertEquals("Other", other.displayName().orElseThrow());
    }

    @Test
    void invalidOverlayDoesNotApply() throws Exception {
        Path defaults = temp.resolve("default.yml");
        Path overlay = temp.resolve("user.yml");
        Files.writeString(defaults, """
                id: skills
                title: Default Title
                rows: 3
                """);
        Files.writeString(overlay, "rows: 0\n");

        GraniteTemplateValidationException ex = assertThrows(
                GraniteTemplateValidationException.class,
                () -> GraniteMenuOverlay.applyFiles(defaults, overlay));
        assertTrue(ex.errors().stream().anyMatch(error -> error.contains("rows")));
    }
}
