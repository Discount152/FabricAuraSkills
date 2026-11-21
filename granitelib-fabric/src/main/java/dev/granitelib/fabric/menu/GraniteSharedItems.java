package dev.granitelib.fabric.menu;

import dev.granitelib.fabric.template.model.GraniteTemplateItem;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * One item definition that more than one menu can reference. Each reference chooses its own slot.
 */
public final class GraniteSharedItems {

    private final Map<String, GraniteTemplateItem> items = new LinkedHashMap<>();

    public void define(String id, String material, String displayName, String action) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("shared item id is required");
        }
        items.put(id, new GraniteTemplateItem(
                List.of(0),
                material,
                optional(displayName),
                List.of(),
                OptionalInt.empty(),
                OptionalInt.empty(),
                optional(action)));
    }

    public GraniteTemplateItem reference(String id, int slot) {
        GraniteTemplateItem shared = items.get(id);
        if (shared == null) {
            throw new IllegalArgumentException("unknown shared item '" + id + "'");
        }
        return new GraniteTemplateItem(
                List.of(slot),
                shared.material(),
                shared.displayName(),
                shared.lore(),
                shared.amount(),
                shared.customModelData(),
                shared.action());
    }

    private static Optional<String> optional(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(value);
    }
}
