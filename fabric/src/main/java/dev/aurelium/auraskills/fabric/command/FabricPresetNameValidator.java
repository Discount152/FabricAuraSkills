package dev.aurelium.auraskills.fabric.command;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Validates preset zip <strong>tokens</strong> as single-segment names under {@code presets/},
 * matching common {@link dev.aurelium.auraskills.common.config.preset.PresetManager#preparePreset}
 * expectations. **Sprint 62.**
 */
public final class FabricPresetNameValidator {

    private FabricPresetNameValidator() {}

    /**
     * @return empty if {@code name} is a safe basename ending in {@code .zip}; otherwise a reason
     *     tag
     */
    public static Optional<String> validateZipFileName(String name) {
        if (name == null || name.isBlank()) {
            return Optional.of("empty");
        }
        if (name.contains("..")
                || name.indexOf('/') >= 0
                || name.indexOf('\\') >= 0
                || name.indexOf(':') >= 0
                || name.startsWith(".")
                || name.indexOf('\0') >= 0) {
            return Optional.of("path");
        }
        if (!name.endsWith(".zip")) {
            return Optional.of("ext");
        }
        if (!name.equals(new File(name).getName())) {
            return Optional.of("not_basename");
        }
        return Optional.empty();
    }

    /**
     * Resolves to a file under {@code pluginFolder/presets/} only (normalized absolute paths).
     */
    public static @Nullable File resolveSafePresetZip(File pluginFolder, String fileName) {
        if (validateZipFileName(fileName).isPresent()) {
            return null;
        }
        try {
            File dir = new File(pluginFolder, "presets");
            if (!dir.isDirectory() && !dir.exists()) {
                return null;
            }
            File candidate = new File(dir, fileName);
            Path dirPath = dir.toPath().toAbsolutePath().normalize();
            Path filePath = candidate.toPath().toAbsolutePath().normalize();
            if (!filePath.startsWith(dirPath)) {
                return null;
            }
            return candidate;
        } catch (Exception e) {
            return null;
        }
    }
}
