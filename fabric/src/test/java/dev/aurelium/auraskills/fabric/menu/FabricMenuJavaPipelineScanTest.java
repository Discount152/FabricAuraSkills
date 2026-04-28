package dev.aurelium.auraskills.fabric.menu;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

class FabricMenuJavaPipelineScanTest {

    private static final List<String> SOURCES_RELATIVE =
            List.of(
                    "src/main/java/dev/aurelium/auraskills/fabric/menu/FabricSkillsMenuGui.java",
                    "src/main/java/dev/aurelium/auraskills/fabric/menu/FabricSkillsTemplateSupport.java",
                    "src/main/java/dev/aurelium/auraskills/fabric/menu/FabricStatsMenuGui.java",
                    "src/main/java/dev/aurelium/auraskills/fabric/menu/FabricStatsTemplateSupport.java",
                    "src/main/java/dev/aurelium/auraskills/fabric/menu/FabricListMenuPaging.java",
                    "src/main/java/dev/aurelium/auraskills/fabric/menu/FabricLevelProgressionMenuGui.java",
                    "src/main/java/dev/aurelium/auraskills/fabric/menu/FabricStatInfoMenuGui.java",
                    "src/main/java/dev/aurelium/auraskills/fabric/menu/FabricStatInfoTemplateSupport.java",
                    "src/main/java/dev/aurelium/auraskills/fabric/menu/FabricSourcesMenuGui.java",
                    "src/main/java/dev/aurelium/auraskills/fabric/menu/FabricSourcesTemplateSupport.java",
                    "src/main/java/dev/aurelium/auraskills/fabric/menu/FabricUnclaimedItemsMenuGui.java",
                    "src/main/java/dev/aurelium/auraskills/fabric/menu/FabricUnclaimedItemsTemplateSupport.java",
                    "src/main/java/dev/aurelium/auraskills/fabric/menu/FabricLeaderboardMenuGui.java",
                    "src/main/java/dev/aurelium/auraskills/fabric/menu/FabricLeaderboardTemplateSupport.java",
                    "src/main/java/dev/aurelium/auraskills/fabric/menu/FabricAbilitiesMenuGui.java",
                    "src/main/java/dev/aurelium/auraskills/fabric/menu/FabricAbilitiesTemplateSupport.java",
                    "src/main/java/dev/aurelium/auraskills/fabric/menu/FabricMenuRegistry.java",
                    "src/main/java/dev/aurelium/auraskills/fabric/menu/FabricMenuPresentation.java",
                    "src/main/java/dev/aurelium/auraskills/fabric/menu/FabricGraniteTemplateStacks.java",
                    "src/main/java/dev/aurelium/auraskills/fabric/menu/FabricMenuLoreFormatting.java",
                    "src/main/java/dev/aurelium/auraskills/fabric/menu/FabricMenuItemPresentation.java",
                    "src/main/java/dev/aurelium/auraskills/fabric/menu/FabricMenuTitles.java",
                    "src/main/java/dev/aurelium/auraskills/fabric/menu/FabricLevelProgressionTemplateSupport.java",
                    "src/main/java/dev/aurelium/auraskills/fabric/menu/FabricLevelProgressionMenuContent.java");

    @Test
    void menuSourcesAvoidRegressions() throws Exception {
        Path root = Path.of(System.getProperty("user.dir"));
        for (String rel : SOURCES_RELATIVE) {
            Path f = root.resolve(rel);
            assertTrue(Files.isRegularFile(f), "expected source file: " + f.toAbsolutePath());
            String src = Files.readString(f);
            String fileName = f.getFileName().toString();
            assertFalse(src.contains("Items.GLASS_PANE"), fileName + " must not use glass pane fillers");
            assertFalse(
                    src.contains("Items.GRAY_STAINED_GLASS_PANE"),
                    fileName + " must not use stained glass pane fillers in Java");
            assertFalse(src.contains("\"<underline"), fileName + " must not use raw <underline string literals");
            assertFalse(src.contains("\"<#"), fileName + " must not use raw hex MiniMessage string literals");
            if ("FabricLeaderboardMenuGui.java".equals(fileName)) {
                assertFalse(src.contains("GOLD_NUGGET"), fileName + " must not use gold nugget row icons");
            }
        }
    }
}