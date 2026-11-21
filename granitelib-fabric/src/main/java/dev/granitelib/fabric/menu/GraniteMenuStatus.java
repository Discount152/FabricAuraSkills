package dev.granitelib.fabric.menu;

/**
 * The file that supplied the loaded menu, whether a fallback file was used, and the error from the
 * file that was not used.
 */
public record GraniteMenuStatus(String id, String effectiveFile, boolean fallback, String error) {

    public GraniteMenuStatus {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id is required");
        }
        effectiveFile = effectiveFile == null ? "" : effectiveFile;
        error = error == null ? "" : error;
    }
}
