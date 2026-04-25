package dev.aurelium.auraskills.fabric.command;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FabricPresetNameValidatorTest {

    @Test
    void validateZipFileName_rejectsTraversalAndSeparators() {
        assertTrue(FabricPresetNameValidator.validateZipFileName("../evil.zip").isPresent());
        assertTrue(FabricPresetNameValidator.validateZipFileName("a\\b.zip").isPresent());
        assertTrue(FabricPresetNameValidator.validateZipFileName("a/b.zip").isPresent());
        assertTrue(FabricPresetNameValidator.validateZipFileName("C:\\x.zip").isPresent());
        assertTrue(FabricPresetNameValidator.validateZipFileName("x:.zip").isPresent());
    }

    @Test
    void validateZipFileName_rejectsWrongExtension() {
        assertTrue(FabricPresetNameValidator.validateZipFileName("legacy.yml").isPresent());
    }

    @Test
    void validateZipFileName_rejectsLeadingDot() {
        assertTrue(FabricPresetNameValidator.validateZipFileName(".hidden.zip").isPresent());
    }

    @Test
    void validateZipFileName_rejectsNul() {
        assertTrue(FabricPresetNameValidator.validateZipFileName("a\u0000b.zip").isPresent());
    }

    @Test
    void validateZipFileName_acceptsSimpleZip() {
        assertTrue(FabricPresetNameValidator.validateZipFileName("legacy.zip").isEmpty());
    }

    @Test
    void resolveSafePresetZip_staysUnderPresets(@TempDir Path tmp) throws Exception {
        File plugin = tmp.resolve("config").resolve("auraskills").toFile();
        File presets = new File(plugin, "presets");
        assertTrue(presets.mkdirs());
        File existing = new File(presets, "pack.zip");
        assertTrue(existing.createNewFile());

        File ok = FabricPresetNameValidator.resolveSafePresetZip(plugin, "pack.zip");
        assertNotNull(ok);
        assertEquals(existing.getCanonicalPath(), ok.getCanonicalPath());

        assertNull(FabricPresetNameValidator.resolveSafePresetZip(plugin, "../pack.zip"));
    }
}
