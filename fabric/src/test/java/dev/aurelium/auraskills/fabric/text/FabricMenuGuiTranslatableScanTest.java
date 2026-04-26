package dev.aurelium.auraskills.fabric.text;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Fails if targeted Fabric UI/command sources use {@code Text.translatable("auraskills.granite…")}
 * again — vanilla clients would see raw keys. ({@link FabricGraniteText} sends literals resolved on
 * the server.)
 */
class FabricMenuGuiTranslatableScanTest {

    /** Paths relative to the {@code fabric} project directory (Gradle test working dir). */
    private static final List<String> SOURCES_RELATIVE =
            List.of(
                    "src/main/java/dev/aurelium/auraskills/fabric/menu/FabricSkillsMenuGui.java",
                    "src/main/java/dev/aurelium/auraskills/fabric/menu/FabricStatsMenuGui.java",
                    "src/main/java/dev/aurelium/auraskills/fabric/menu/FabricListMenuPaging.java",
                    "src/main/java/dev/aurelium/auraskills/fabric/menu/FabricLevelProgressionMenuGui.java",
                    "src/main/java/dev/aurelium/auraskills/fabric/menu/FabricStatInfoMenuGui.java",
                    "src/main/java/dev/aurelium/auraskills/fabric/menu/FabricSourcesMenuGui.java",
                    "src/main/java/dev/aurelium/auraskills/fabric/menu/FabricUnclaimedItemsMenuGui.java",
                    "src/main/java/dev/aurelium/auraskills/fabric/menu/FabricLeaderboardMenuGui.java",
                    "src/main/java/dev/aurelium/auraskills/fabric/menu/FabricAbilitiesMenuGui.java",
                    "src/main/java/dev/aurelium/auraskills/fabric/menu/FabricMenuRegistry.java",
                    "src/main/java/dev/aurelium/auraskills/fabric/command/FabricOpenMenuCommands.java",
                    "src/main/java/dev/aurelium/auraskills/fabric/command/FabricOpenMenuAbilities.java",
                    "src/main/java/dev/aurelium/auraskills/fabric/command/FabricOpenMenuLeaderboard.java",
                    "src/main/java/dev/aurelium/auraskills/fabric/command/FabricSourcesOpenSupport.java",
                    "src/main/java/dev/aurelium/auraskills/fabric/command/FabricSourcesCommands.java");

    private static final String BANNED_SNIPPET = "Text.translatable(\"auraskills.granite";

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
                            + " must not use Text.translatable for auraskills.granite keys; use "
                            + "FabricGraniteText.key");
        }
    }
}
