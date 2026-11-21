package dev.granite.graniteskills.common.migration;

import dev.granite.graniteskills.common.util.file.FileUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.spongepowered.configurate.ConfigurationNode;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

    @Test
    void secondRunDoesNotOverwriteAConflictingFile() throws Exception {
        Path oldFolder = temp.resolve("AuraSkills");
        Files.createDirectories(oldFolder.resolve("userdata"));
        Files.writeString(oldFolder.resolve("userdata/player.yml"), "skills:\n  auraskills/farming:\n    level: 2\n");
        Files.writeString(oldFolder.resolve("config.yml"), "default_language: de\n");
        Files.writeString(oldFolder.resolve("rewards.yml"), "source: old\n");

        Path pluginFolder = temp.resolve("graniteskills");
        Files.createDirectories(pluginFolder.resolve("userdata"));
        Files.writeString(pluginFolder.resolve("userdata/player.yml"), "skills:\n  graniteskills/farming:\n    level: 9\n");
        Files.writeString(pluginFolder.resolve("config.yml"), "default_language: en\n");

        List<String> first = IdentityMigrator.migrateFolder(pluginFolder);
        assertTrue(first.stream().anyMatch(line -> line.contains("Migrated")));
        ConfigurationNode player = FileUtil.loadYamlFile(pluginFolder.resolve("userdata/player.yml").toFile());
        assertEquals(9, player.node("skills", "graniteskills/farming", "level").getInt());
        assertTrue(player.node("skills", "auraskills/farming").virtual());
        assertTrue(text(pluginFolder.resolve("config.yml")).contains("default_language: en"));
        assertTrue(text(pluginFolder.resolve("rewards.yml")).contains("source: old"));
        assertTrue(Files.exists(pluginFolder.resolve(LegacyIdentity.MARKER_FILE)));

        Files.writeString(pluginFolder.resolve("config.yml"), "default_language: fr\n");
        Files.writeString(pluginFolder.resolve("userdata/player.yml"), "skills:\n  graniteskills/farming:\n    level: 11\n");
        Files.writeString(pluginFolder.resolve("rewards.yml"), "source: edited\n");

        List<String> second = IdentityMigrator.migrateFolder(pluginFolder);
        assertTrue(second.stream().anyMatch(line -> line.contains("already migrated")));
        assertFalse(second.stream().anyMatch(line -> line.contains("Migrated")));
        assertTrue(text(pluginFolder.resolve("config.yml")).contains("default_language: fr"));
        assertTrue(text(pluginFolder.resolve("rewards.yml")).contains("source: edited"));
        player = FileUtil.loadYamlFile(pluginFolder.resolve("userdata/player.yml").toFile());
        assertEquals(11, player.node("skills", "graniteskills/farming", "level").getInt());
        assertTrue(text(oldFolder.resolve("userdata/player.yml")).contains("level: 2"));
    }

    private static String text(Path path) throws Exception {
        return Files.readString(path);
    }

}
