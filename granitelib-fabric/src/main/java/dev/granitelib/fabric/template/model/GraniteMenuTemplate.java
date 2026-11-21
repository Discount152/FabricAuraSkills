package dev.granitelib.fabric.template.model;

import java.util.Map;
import java.util.Optional;

/**
 * Immutable v0 menu template (read-only load). Not wired to live screens in Spike 0.
 */
public record GraniteMenuTemplate(
        String id,
        String title,
        int rows,
        Optional<GraniteTemplateFill> fill,
        Map<String, GraniteTemplateItem> items,
        Optional<String> sourceLabel) {

    public GraniteMenuTemplate {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id must be non-blank");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title must be non-blank");
        }
        if (rows < 1 || rows > 6) {
            throw new IllegalArgumentException("rows must be between 1 and 6, got " + rows);
        }
        items = Map.copyOf(items);
    }

    /** Inclusive upper bound for slot indices: {@code rows * 9 - 1}. */
    public int maxSlotIndex() {
        return rows * 9 - 1;
    }
}
