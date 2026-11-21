package dev.granitelib.fabric.menu;

import dev.granitelib.fabric.template.model.GraniteMenuTemplate;
import dev.granitelib.fabric.template.model.GraniteTemplateFill;
import dev.granitelib.fabric.template.model.GraniteTemplateItem;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Builds a menu and loads it. The result carries the title, row count, and item slots given here.
 */
public final class GraniteMenuBuilder {

    private final String id;
    private String title = "";
    private int rows;
    private Optional<GraniteTemplateFill> fill = Optional.empty();
    private final Map<String, GraniteTemplateItem> items = new LinkedHashMap<>();

    private GraniteMenuBuilder(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id is required");
        }
        this.id = id;
    }

    public static GraniteMenuBuilder menu(String id) {
        return new GraniteMenuBuilder(id);
    }

    public GraniteMenuBuilder title(String title) {
        this.title = title == null ? "" : title;
        return this;
    }

    public GraniteMenuBuilder rows(int rows) {
        this.rows = rows;
        return this;
    }

    public GraniteMenuBuilder fill(String material) {
        if (material == null || material.isBlank()) {
            throw new IllegalArgumentException("fill material is required");
        }
        this.fill = Optional.of(new GraniteTemplateFill(material, Optional.empty()));
        return this;
    }

    public GraniteMenuBuilder item(String key, String material, int... slots) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("item key is required");
        }
        if (material == null || material.isBlank()) {
            throw new IllegalArgumentException("item material is required");
        }
        if (slots.length == 0) {
            throw new IllegalArgumentException("item slots are required");
        }
        List<Integer> slotList = new ArrayList<>();
        for (int slot : slots) {
            slotList.add(slot);
        }
        items.put(key, new GraniteTemplateItem(
                slotList, material, Optional.empty(), List.of(), java.util.OptionalInt.empty(),
                java.util.OptionalInt.empty(), Optional.empty()));
        return this;
    }

    public GraniteMenuTemplate build() {
        if (title.isBlank()) {
            throw new IllegalArgumentException("title is required");
        }
        if (rows < 1 || rows > 6) {
            throw new IllegalArgumentException("rows must be between 1 and 6, got " + rows);
        }
        int maxSlot = rows * 9 - 1;
        for (Map.Entry<String, GraniteTemplateItem> entry : items.entrySet()) {
            for (int slot : entry.getValue().slots()) {
                if (slot < 0 || slot > maxSlot) {
                    throw new IllegalArgumentException("slot " + slot + " is outside rows=" + rows);
                }
            }
        }
        return new GraniteMenuTemplate(id, title, rows, fill, items, Optional.of("builder"));
    }

    public void loadInto(GraniteLoadedMenuRegistry registry, GraniteMenuListener listener) {
        registry.load(build(), listener);
    }
}
