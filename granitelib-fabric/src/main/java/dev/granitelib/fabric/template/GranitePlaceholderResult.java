package dev.granitelib.fabric.template;

import java.util.List;

/** A display name after placeholder replacement, plus the placeholders that had no value. */
public record GranitePlaceholderResult(String text, List<String> unknown) {

    public GranitePlaceholderResult {
        text = text == null ? "" : text;
        unknown = unknown == null ? List.of() : List.copyOf(unknown);
    }
}
