package dev.granite.graniteskills.common.migration;

import dev.granite.graniteskills.common.util.file.FileUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.spongepowered.configurate.ConfigurationNode;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IdentityMigratorTest {

    @TempDir
    Path temp;

    @Test
    void copiesOldFolderAndRewritesStoredIds() throws Exception {
        Path oldFolder = temp.resolve("auraskills");
        Path userdata = oldFolder.resolve("userdata");
        Files.createDirectories(userdata);
        Files.writeString(oldFolder.resolve("config.yml"), "default_language: de\nsql:\n  database: auraskills\n");
        Files.writeString(userdata.resolve("player.yml"), """
                skills:
                  auraskills/mining:
                    level: 5
                    xp: 1.5
                modifiers:
                  - name: AureliumSkills.Ability.Revival.Health
                note: "AuraSkills is the old name"
                """);

        Path pluginFolder = temp.resolve("graniteskills");
        Files.createDirectories(pluginFolder);
        Files.writeString(pluginFolder.resolve("config.yml"), "default_language: en\n");

        IdentityMigrator.migrateFolder(pluginFolder);

        assertTrue(Files.exists(oldFolder.resolve("config.yml")));
        assertTrue(text(pluginFolder.resolve("config.yml")).contains("default_language: de"));
        assertTrue(text(pluginFolder.resolve("config.yml")).contains("database: auraskills"));
        ConfigurationNode player = FileUtil.loadYamlFile(pluginFolder.resolve("userdata/player.yml").toFile());
        assertEquals(5, player.node("skills", "graniteskills/mining", "level").getInt());
        assertTrue(player.node("skills", "auraskills/mining").virtual());
        assertEquals("GraniteSkills.Ability.Revival.Health", player.node("modifiers").childrenList().get(0).node("name").getString());
        assertEquals("AuraSkills is the old name", player.node("note").getString());
        assertTrue(Files.exists(pluginFolder.resolve(LegacyIdentity.MARKER_FILE)));

        Files.writeString(pluginFolder.resolve("config.yml"), "default_language: fr\n");
        IdentityMigrator.migrateFolder(pluginFolder);
        assertTrue(text(pluginFolder.resolve("config.yml")).contains("default_language: fr"));
    }

    @Test
    void keepsExistingPlayerFilesWhenTheNewFolderAlreadyHasUserdata() throws Exception {
        Path oldFolder = temp.resolve("AuraSkills");
        Files.createDirectories(oldFolder.resolve("userdata"));
        Files.writeString(oldFolder.resolve("userdata/old.yml"), "skills:\n  auraskills/farming:\n    level: 2\n");

        Path pluginFolder = temp.resolve("GraniteSkills");
        Files.createDirectories(pluginFolder.resolve("userdata"));
        Files.writeString(pluginFolder.resolve("userdata/old.yml"), "skills:\n  graniteskills/farming:\n    level: 9\n");
        Files.writeString(oldFolder.resolve("extra.yml"), "kept: true\n");

        IdentityMigrator.migrateFolder(pluginFolder);

        ConfigurationNode player = FileUtil.loadYamlFile(pluginFolder.resolve("userdata/old.yml").toFile());
        assertEquals(9, player.node("skills", "graniteskills/farming", "level").getInt());
        assertTrue(player.node("skills", "auraskills/farming").virtual());
        assertTrue(text(pluginFolder.resolve("extra.yml")).contains("kept: true"));
    }

    private static String text(Path path) throws Exception {
        return Files.readString(path);
    }

}
