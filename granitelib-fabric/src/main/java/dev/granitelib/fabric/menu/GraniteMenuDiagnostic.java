package dev.granitelib.fabric.menu;

/** One reason an invalid menu file was rejected. The file is the file that failed, not a fallback. */
public record GraniteMenuDiagnostic(String file, String reason) {

    public GraniteMenuDiagnostic {
        if (file == null || file.isBlank()) {
            throw new IllegalArgumentException("file is required");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("reason is required");
        }
    }
}
