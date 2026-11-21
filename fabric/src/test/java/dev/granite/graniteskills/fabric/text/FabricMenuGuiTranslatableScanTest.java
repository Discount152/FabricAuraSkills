package dev.granite.graniteskills.fabric.text;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Fails if targeted Fabric UI/command sources use {@code Component.translatable("graniteskills.granite…")}
 * again — vanilla clients would see raw keys. ({@link FabricGraniteText} sends literals resolved on
 * the server.)
 */
class FabricMenuGuiTranslatableScanTest {

    /** Paths relative to the {@code fabric} project directory (Gradle test working dir). */
    private static final List<String> SOURCES_RELATIVE =
            List.of(
                    "src/main/java/dev/granite/graniteskills/fabric/menu/FabricSkillsMenuGui.java",
                    "src/main/java/dev/granite/graniteskills/fabric/menu/FabricSkillsTemplateSupport.java",
                    "src/main/java/dev/granite/graniteskills/fabric/menu/FabricStatsMenuGui.java",
                    "src/main/java/dev/granite/graniteskills/fabric/menu/FabricStatsTemplateSupport.java",
                    "src/main/java/dev/granite/graniteskills/fabric/menu/FabricListMenuPaging.java",
                    "src/main/java/dev/granite/graniteskills/fabric/menu/FabricLevelProgressionMenuGui.java",
                    "src/main/java/dev/granite/graniteskills/fabric/menu/FabricStatInfoMenuGui.java",
                    "src/main/java/dev/granite/graniteskills/fabric/menu/FabricSourcesMenuGui.java",
                    "src/main/java/dev/granite/graniteskills/fabric/menu/FabricUnclaimedItemsMenuGui.java",
                    "src/main/java/dev/granite/graniteskills/fabric/menu/FabricUnclaimedItemsTemplateSupport.java",
                    "src/main/java/dev/granite/graniteskills/fabric/menu/FabricLeaderboardMenuGui.java",
                    "src/main/java/dev/granite/graniteskills/fabric/menu/FabricLeaderboardTemplateSupport.java",
                    "src/main/java/dev/granite/graniteskills/fabric/menu/FabricAbilitiesMenuGui.java",
                    "src/main/java/dev/granite/graniteskills/fabric/menu/FabricAbilitiesTemplateSupport.java",
                    "src/main/java/dev/granite/graniteskills/fabric/menu/FabricMenuRegistry.java",
                    "src/main/java/dev/granite/graniteskills/fabric/command/FabricOpenMenuCommands.java",
                    "src/main/java/dev/granite/graniteskills/fabric/command/FabricOpenMenuAbilities.java",
                    "src/main/java/dev/granite/graniteskills/fabric/command/FabricOpenMenuLeaderboard.java",
                    "src/main/java/dev/granite/graniteskills/fabric/command/FabricSourcesOpenSupport.java",
                    "src/main/java/dev/granite/graniteskills/fabric/command/FabricSourcesCommands.java");

    private static final String BANNED_SNIPPET = "Component.translatable(\"graniteskills.granite";

    @Test
    void menuAndOpenMenuSourcesDoNotUseTranslatableForGraniteKeys() throws Exception {
        Path root = Path.of(System.getProperty("user.dir"));
        for (String rel : SOURCES_RELATIVE) {
            Path f = root.resolve(rel);
            assertTrue(Files.isRegularFile(f), "expected source file: " + f.toAbsolutePath());
            String src = Files.readString(f);
            assertFalse(
                    src.contains(BANNED_SNIPPET),
                    f
                            + " must not use Component.translatable for graniteskills.granite keys; use "
                            + "FabricGraniteText.key");
        }
    }
}
