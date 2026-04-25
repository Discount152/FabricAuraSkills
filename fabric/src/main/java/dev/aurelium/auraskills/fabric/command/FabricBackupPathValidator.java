package dev.aurelium.auraskills.fabric.command;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Rejects path traversal and out-of-tree backup targets. **Sprint 61.**
 */
public final class FabricBackupPathValidator {

    private FabricBackupPathValidator() {}

    /**
     * @return empty if the token is a safe single-segment {@code .yml} name; otherwise a reason for
     *     logging / generic errors
     */
    public static Optional<String> validateFileNameToken(String name) {
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
        if (!name.endsWith(".yml")) {
            return Optional.of("ext");
        }
        if (!name.equals(new File(name).getName())) {
            return Optional.of("not_basename");
        }
        return Optional.empty();
    }

    /**
     * Resolves {@code fileName} to a file under {@code pluginFolder/backups/} only, using
     * normalized paths (canonical on supported platforms).
     */
    public static @Nullable File resolveSafeBackupFile(File pluginFolder, String fileName) {
        if (validateFileNameToken(fileName).isPresent()) {
            return null;
        }
        try {
            File dir = new File(pluginFolder, "backups");
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
