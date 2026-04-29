package dev.aurelium.auraskills.fabric.menu;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Sprint 122: bundled template scan, embedded menu defaults, blank display-name routing.
 */
class FabricSprint122RuntimeMenuRegressionTest {

    private static final List<String> FORBIDDEN_GLASS =
            List.of("gray_stained_glass_pane", "minecraft:glass_pane", "minecraft:white_stained_glass_pane");

    private static final List<String> EMBEDDED_MENU_FILES =
            List.of("menus/skills.yml", "menus/stats.yml", "menus/stat_info.yml", "menus/level_progression.yml");

    @Test
    void bundledGraniteTemplates_containNoGlobalGlassMaterials() throws Exception {
        Path root = Path.of(System.getProperty("user.dir"));
        Path dir = root.resolve("src/main/resources/assets/auraskills/granite/templates");
        assertTrue(Files.isDirectory(dir), "expected template dir: " + dir.toAbsolutePath());
        try (Stream<Path> paths = Files.list(dir)) {
            List<Path> ymls = paths.filter(p -> p.getFileName().toString().endsWith(".yml")).toList();
            assertFalse(ymls.isEmpty(), "expected bundled templates");
            for (Path f : ymls) {
                String s = Files.readString(f);
                for (String bad : FORBIDDEN_GLASS) {
                    assertFalse(s.contains(bad), f.getFileName() + " must not contain " + bad);
                }
            }
        }
    }

    @Test
    void embeddedBuiltInMenuYaml_backItemUsesIronDoor() throws Exception {
        Path root = Path.of(System.getProperty("user.dir"));
        for (String rel : EMBEDDED_MENU_FILES) {
            Path f = root.resolve("src/main/resources").resolve(rel);
            assertTrue(Files.isRegularFile(f), "missing " + f.toAbsolutePath());
            String yaml = Files.readString(f);
            assertTrue(yaml.contains("back: \"minecraft:iron_door\""), rel);
            assertFalse(yaml.contains("back: \"minecraft:arrow\""), rel);
        }
    }

    @Test
    void bundledGraniteTemplates_backBlockNeverUsesArrowMaterial() throws Exception {
        Path root = Path.of(System.getProperty("user.dir"));
        Path dir = root.resolve("src/main/resources/assets/auraskills/granite/templates");
        try (Stream<Path> paths = Files.list(dir)) {
            for (Path f : paths.filter(p -> p.getFileName().toString().endsWith(".yml")).toList()) {
                String s = Files.readString(f);
                assertFalse(
                        s.contains("  back:\n    material: minecraft:arrow"),
                        f.getFileName() + ": Back must use iron_door, not arrow");
            }
        }
    }

    @Test
    void blankTemplateDisplayName_detection() {
        assertTrue(FabricGraniteTemplateStacks.isBlankTemplateDisplayName(null));
        assertTrue(FabricGraniteTemplateStacks.isBlankTemplateDisplayName(""));
        assertTrue(FabricGraniteTemplateStacks.isBlankTemplateDisplayName(" "));
        assertTrue(FabricGraniteTemplateStacks.isBlankTemplateDisplayName("  \t "));
        assertFalse(FabricGraniteTemplateStacks.isBlankTemplateDisplayName("x"));
        assertFalse(FabricGraniteTemplateStacks.isBlankTemplateDisplayName(" a "));
    }
}
