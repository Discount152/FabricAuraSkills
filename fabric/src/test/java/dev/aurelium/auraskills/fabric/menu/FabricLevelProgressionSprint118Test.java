package dev.aurelium.auraskills.fabric.menu;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FabricLevelProgressionSprint118Test {

    @Test
    void layoutConstantsUnchanged() {
        assertEquals(24, FabricLevelProgressionLayout.PAGE_SIZE);
        assertEquals(45, FabricLevelProgressionLayout.SLOT_BACK);
        assertEquals(48, FabricLevelProgressionLayout.SLOT_SOURCES);
        assertEquals(49, FabricLevelProgressionLayout.SLOT_ABILITIES);
        assertEquals(52, FabricLevelProgressionLayout.SLOT_PREVIOUS_PAGE);
        assertEquals(53, FabricLevelProgressionLayout.SLOT_NEXT_PAGE);
        assertEquals(24, FabricLevelProgressionLayout.LEVEL_PATH_SLOTS.size());
        assertEquals(9, FabricLevelProgressionLayout.LEVEL_PATH_SLOTS.getFirst());
        assertEquals(44, FabricLevelProgressionLayout.LEVEL_PATH_SLOTS.getLast());
    }

    @Test
    void builtInMenuGuiDoesNotUseLegacySourcesHopperSlot() throws IOException {
        Path cwd = Path.of(System.getProperty("user.dir"));
        Path gui =
                cwd.resolve(
                        "src/main/java/dev/aurelium/auraskills/fabric/menu/FabricLevelProgressionMenuGui.java");
        if (!Files.isRegularFile(gui)) {
            gui =
                    cwd.resolve(
                            "fabric/src/main/java/dev/aurelium/auraskills/fabric/menu/FabricLevelProgressionMenuGui.java");
        }
        assertTrue(Files.isRegularFile(gui), "expected FabricLevelProgressionMenuGui under " + cwd.toAbsolutePath());
        String src = Files.readString(gui);
        assertFalse(src.contains("SLOT_OPEN_SOURCES"), "remove legacy slot-50 sources opener");
        assertTrue(src.contains("FabricLevelProgressionLayout.SLOT_SOURCES"));
        assertTrue(src.contains("openBuiltInSnake"), "built-in path should be snake-layout parity");
        assertTrue(
                src.contains("FabricLevelProgressionLayout.PAGE_SIZE"),
                "built-in paging must use FabricLevelProgressionLayout.PAGE_SIZE");
        assertTrue(
                src.contains("FabricLevelProgressionLayout.LEVEL_PATH_SLOTS"),
                "built-in path placement must use LEVEL_PATH_SLOTS");
    }
}