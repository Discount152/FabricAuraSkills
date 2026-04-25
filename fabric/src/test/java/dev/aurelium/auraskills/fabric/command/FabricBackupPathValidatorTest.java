package dev.aurelium.auraskills.fabric.command;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FabricBackupPathValidatorTest {

    @Test
    void validateFileNameToken_rejectsTraversalAndSeparators() {
        assertTrue(FabricBackupPathValidator.validateFileNameToken("../secret.yml").isPresent());
        assertTrue(FabricBackupPathValidator.validateFileNameToken("..\\foo.yml").isPresent());
        assertTrue(FabricBackupPathValidator.validateFileNameToken("a/b.yml").isPresent());
        assertTrue(FabricBackupPathValidator.validateFileNameToken("C:\\x.yml").isPresent());
        assertTrue(FabricBackupPathValidator.validateFileNameToken("x:.yml").isPresent());
    }

    @Test
    void validateFileNameToken_rejectsWrongExtension() {
        assertTrue(FabricBackupPathValidator.validateFileNameToken("backup.txt").isPresent());
    }

    @Test
    void validateFileNameToken_acceptsSimpleYml() {
        assertTrue(
                FabricBackupPathValidator.validateFileNameToken("backup-2026-04-24_14-30-00.yml")
                        .isEmpty());
    }

    @Test
    void resolveSafeBackupFile_staysUnderBackups(@TempDir Path tmp) throws Exception {
        File plugin = tmp.resolve("config").resolve("auraskills").toFile();
        File backups = new File(plugin, "backups");
        assertTrue(backups.mkdirs());
        File existing = new File(backups, "good.yml");
        assertTrue(existing.createNewFile());

        File ok =
                FabricBackupPathValidator.resolveSafeBackupFile(
                        plugin, "good.yml");
        assertNotNull(ok);
        assertEquals(existing.getCanonicalPath(), ok.getCanonicalPath());

        assertNull(
                FabricBackupPathValidator.resolveSafeBackupFile(plugin, "../good.yml"));

        File safeButMissing =
                FabricBackupPathValidator.resolveSafeBackupFile(plugin, "not-created.yml");
        assertNotNull(safeButMissing);
        assertTrue(safeButMissing.getPath().endsWith("not-created.yml"));
        assertFalse(safeButMissing.isFile());
    }
}
