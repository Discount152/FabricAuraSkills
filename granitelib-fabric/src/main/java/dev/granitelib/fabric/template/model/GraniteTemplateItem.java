package dev.granitelib.fabric.template.model;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Static template item (v0). {@code action} is a placeholder key only — no dispatch in Spike 0.
 */
public record GraniteTemplateItem(
        List<Integer> slots,
        String material,
        Optional<String> displayName,
        List<String> lore,
        OptionalInt amount,
        OptionalInt customModelData,
        Optional<String> action) {

    public GraniteTemplateItem {
        slots = List.copyOf(slots);
        lore = List.copyOf(lore);
        if (slots.isEmpty()) {
            throw new IllegalArgumentException("item.slots must not be empty");
        }
        if (material == null || material.isBlank()) {
            throw new IllegalArgumentException("item.material must be non-blank");
        }
    }
}
