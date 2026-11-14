package dev.granite.graniteskills.fabric.menu;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FabricMenuJavaPipelineScanTest {

    private static final List<String> SOURCES_RELATIVE =
            List.of(
                    "src/main/java/dev/granite/graniteskills/fabric/menu/FabricSkillsMenuGui.java",
                    "src/main/java/dev/granite/graniteskills/fabric/menu/FabricSkillsTemplateSupport.java",
                    "src/main/java/dev/granite/graniteskills/fabric/menu/FabricStatsMenuGui.java",
                    "src/main/java/dev/granite/graniteskills/fabric/menu/FabricStatsTemplateSupport.java",
                    "src/main/java/dev/granite/graniteskills/fabric/menu/FabricListMenuPaging.java",
                    "src/main/java/dev/granite/graniteskills/fabric/menu/FabricLevelProgressionMenuGui.java",
                    "src/main/java/dev/granite/graniteskills/fabric/menu/FabricStatInfoMenuGui.java",
                    "src/main/java/dev/granite/graniteskills/fabric/menu/FabricStatInfoTemplateSupport.java",
                    "src/main/java/dev/granite/graniteskills/fabric/menu/FabricSourcesMenuGui.java",
                    "src/main/java/dev/granite/graniteskills/fabric/menu/FabricSourcesTemplateSupport.java",
                    "src/main/java/dev/granite/graniteskills/fabric/menu/FabricUnclaimedItemsMenuGui.java",
                    "src/main/java/dev/granite/graniteskills/fabric/menu/FabricUnclaimedItemsTemplateSupport.java",
                    "src/main/java/dev/granite/graniteskills/fabric/menu/FabricLeaderboardMenuGui.java",
                    "src/main/java/dev/granite/graniteskills/fabric/menu/FabricLeaderboardTemplateSupport.java",
                    "src/main/java/dev/granite/graniteskills/fabric/menu/FabricAbilitiesMenuGui.java",
                    "src/main/java/dev/granite/graniteskills/fabric/menu/FabricAbilitiesTemplateSupport.java",
                    "src/main/java/dev/granite/graniteskills/fabric/menu/FabricMenuRegistry.java",
                    "src/main/java/dev/granite/graniteskills/fabric/menu/FabricMenuPresentation.java",
                    "src/main/java/dev/granite/graniteskills/fabric/menu/FabricGraniteTemplateStacks.java",
                    "src/main/java/dev/granite/graniteskills/fabric/menu/FabricMenuLoreFormatting.java",
                    "src/main/java/dev/granite/graniteskills/fabric/menu/FabricMenuItemPresentation.java",
                    "src/main/java/dev/granite/graniteskills/fabric/menu/FabricMenuTitles.java",
                    "src/main/java/dev/granite/graniteskills/fabric/menu/FabricLevelProgressionTemplateSupport.java",
                    "src/main/java/dev/granite/graniteskills/fabric/menu/FabricLevelProgressionMenuContent.java");

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
