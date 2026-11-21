package dev.granitelib.fabric.menu;

import dev.granitelib.fabric.template.model.GraniteMenuTemplate;
import dev.granitelib.fabric.template.model.GraniteTemplateItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Repeats one template row for each supplied entry. Every later entry is the same slots shifted
 * down by one row.
 */
public final class GraniteDynamicRows {

    private GraniteDynamicRows() {
    }

    public record RepeatedSlot(String entry, int slot) {
    }

    public static List<RepeatedSlot> expand(GraniteMenuTemplate template, String itemKey, List<String> entries) {
        GraniteTemplateItem pattern = template.items().get(itemKey);
        if (pattern == null) {
            throw new IllegalArgumentException("no dynamic row '" + itemKey + "'");
        }
        if (entries == null || entries.isEmpty()) {
            return List.of();
        }
        int row = pattern.slots().getFirst() / 9;
        for (int slot : pattern.slots()) {
            if (slot / 9 != row) {
                throw new IllegalArgumentException("dynamic row '" + itemKey + "' must occupy one row");
            }
        }
        int max = template.rows() * 9;
        List<RepeatedSlot> repeated = new ArrayList<>();
        for (int index = 0; index < entries.size(); index++) {
            String entry = entries.get(index);
            if (entry == null || entry.isBlank()) {
                throw new IllegalArgumentException("entry is required");
            }
            for (int slot : pattern.slots()) {
                int placed = slot + index * 9;
                if (placed < 0 || placed >= max) {
                    throw new IllegalArgumentException("entry '" + entry + "' does not fit in the menu");
                }
                repeated.add(new RepeatedSlot(entry, placed));
            }
        }
        return List.copyOf(repeated);
    }
}
