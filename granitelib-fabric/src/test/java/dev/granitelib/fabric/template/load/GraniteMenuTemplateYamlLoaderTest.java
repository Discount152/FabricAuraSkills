package dev.granitelib.fabric.template.load;

import dev.granitelib.fabric.template.model.GraniteMenuTemplate;
import dev.granitelib.fabric.template.model.GraniteTemplateItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraniteMenuTemplateYamlLoaderTest {

    @Test
    void parsesMinimalValid() {
        String yaml =
                """
                id: sources
                title: Sources
                rows: 6
                """;
        GraniteMenuTemplate t = GraniteMenuTemplateYamlLoader.loadString(yaml, "test");
        assertEquals("sources", t.id());
        assertEquals("Sources", t.title());
        assertEquals(6, t.rows());
        assertTrue(t.fill().isEmpty());
        assertTrue(t.items().isEmpty());
    }

    @Test
    void parsesStaticItemWithLoreSlotsFillAndAction() {
        String yaml =
                """
                id: sources
                title: "Sources"
                rows: 6
                fill:
                  material: minecraft:black_stained_glass_pane
                  name: " "
                items:
                  back:
                    material: minecraft:arrow
                    slots: [45]
                    name: "Back"
                    lore:
                      - "Return to the previous menu"
                    action: back
                  sort:
                    material: minecraft:hopper
                    slots: [49]
                    name: "Sort"
                    action: cycle_sort
                """;
        GraniteMenuTemplate t = GraniteMenuTemplateYamlLoader.loadString(yaml, "inline");
        assertTrue(t.fill().isPresent());
        assertEquals("minecraft:black_stained_glass_pane", t.fill().get().material());
        GraniteTemplateItem back = t.items().get("back");
        assertEquals(45, back.slots().getFirst());
        assertEquals(1, back.lore().size());
        assertEquals("back", back.action().orElseThrow());
        assertTrue(t.items().get("sort").action().isPresent());
    }

    @Test
    void parsesMultipleSlots() {
        String yaml =
                """
                id: m
                title: T
                rows: 6
                items:
                  x:
                    material: minecraft:stone
                    slots: [10, 11, 12]
                """;
        GraniteMenuTemplate t = GraniteMenuTemplateYamlLoader.loadString(yaml, "t");
        assertEquals(3, t.items().get("x").slots().size());
    }

    @Test
    void parsesSlotSingularKey() {
        String yaml =
                """
                id: m
                title: T
                rows: 6
                items:
                  x:
                    material: minecraft:stone
                    slot: 3
                """;
        GraniteMenuTemplate t = GraniteMenuTemplateYamlLoader.loadString(yaml, "t");
        assertEquals(3, t.items().get("x").slots().getFirst());
    }

    @Test
    void loadPath_readsFile(@TempDir Path dir) throws IOException {
        Path f = dir.resolve("menu.yml");
        Files.writeString(
                f,
                """
                id: path_menu
                title: From Path
                rows: 3
                """);
        GraniteMenuTemplate t = GraniteMenuTemplateYamlLoader.loadPath(f);
        assertEquals("path_menu", t.id());
        assertTrue(t.sourceLabel().isPresent());
    }

    @Test
    void invalidRowsTooLow() {
        GraniteTemplateValidationException ex =
                assertThrows(
                        GraniteTemplateValidationException.class,
                        () ->
                                GraniteMenuTemplateYamlLoader.loadString(
                                        """
                                        id: a
                                        title: b
                                        rows: 0
                                        """,
                                        "t"));
        assertTrue(ex.errors().stream().anyMatch(s -> s.contains("rows")));
    }

    @Test
    void invalidRowsTooHigh() {
        assertThrows(
                GraniteTemplateValidationException.class,
                () ->
                        GraniteMenuTemplateYamlLoader.loadString(
                                """
                                id: a
                                title: b
                                rows: 7
                                """,
                                "t"));
    }

    @Test
    void outOfBoundsSlot() {
        GraniteTemplateValidationException ex =
                assertThrows(
                        GraniteTemplateValidationException.class,
                        () ->
                                GraniteMenuTemplateYamlLoader.loadString(
                                        """
                                        id: a
                                        title: b
                                        rows: 1
                                        items:
                                          x:
                                            material: minecraft:stone
                                            slots: [9]
                                        """,
                                        "t"));
        assertTrue(ex.errors().stream().anyMatch(s -> s.contains("out of bounds")));
    }

    @Test
    void missingId() {
        GraniteTemplateValidationException ex =
                assertThrows(
                        GraniteTemplateValidationException.class,
                        () ->
                                GraniteMenuTemplateYamlLoader.loadString(
                                        """
                                        title: b
                                        rows: 3
                                        """,
                                        "t"));
        assertTrue(ex.errors().stream().anyMatch(s -> s.contains("id")));
    }

    @Test
    void missingTitle() {
        GraniteTemplateValidationException ex =
                assertThrows(
                        GraniteTemplateValidationException.class,
                        () ->
                                GraniteMenuTemplateYamlLoader.loadString(
                                        """
                                        id: a
                                        rows: 3
                                        """,
                                        "t"));
        assertTrue(ex.errors().stream().anyMatch(s -> s.contains("title")));
    }

    @Test
    void missingRows() {
        GraniteTemplateValidationException ex =
                assertThrows(
                        GraniteTemplateValidationException.class,
                        () ->
                                GraniteMenuTemplateYamlLoader.loadString(
                                        """
                                        id: a
                                        title: b
                                        """,
                                        "t"));
        assertTrue(ex.errors().stream().anyMatch(s -> s.contains("rows")));
    }

    @Test
    void rootMustBeMapping() {
        assertThrows(
                GraniteTemplateValidationException.class,
                () -> GraniteMenuTemplateYamlLoader.loadString("- a\n- b", "bad"));
    }

    @Test
    void materialMustBeStringNotResolved() {
        // Strings only; no ItemStack resolution in Spike 0
        GraniteMenuTemplate t =
                GraniteMenuTemplateYamlLoader.loadString(
                        """
                        id: a
                        title: b
                        rows: 2
                        items:
                          z:
                            material: "not_a_valid_mc_id_but_ok"
                            slots: [0]
                        """,
                        "t");
        assertEquals("not_a_valid_mc_id_but_ok", t.items().get("z").material());
    }

    @Test
    void optionalCustomModelData() {
        GraniteMenuTemplate t =
                GraniteMenuTemplateYamlLoader.loadString(
                        """
                        id: a
                        title: b
                        rows: 2
                        items:
                          z:
                            material: minecraft:diamond
                            slots: [0]
                            custom_model_data: 42
                        """,
                        "t");
        assertEquals(42, t.items().get("z").customModelData().getAsInt());
    }
}
