package dev.granitelib.fabric.template.load;

import dev.granitelib.fabric.template.model.GraniteMenuTemplate;
import dev.granitelib.fabric.template.model.GraniteTemplateFill;
import dev.granitelib.fabric.template.model.GraniteTemplateItem;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Applies a partial user menu file over a validated default menu. Keys the overlay omits stay at
 * their default values. Keys it sets replace those values.
 */
public final class GraniteMenuOverlay {

    private GraniteMenuOverlay() {
    }

    public static GraniteMenuTemplate applyFiles(Path defaultFile, Path overlayFile)
            throws IOException, GraniteTemplateValidationException {
        GraniteMenuTemplate base = GraniteMenuTemplateYamlLoader.loadPath(defaultFile);
        String overlay = Files.readString(overlayFile, StandardCharsets.UTF_8);
        return apply(base, overlay, overlayFile.toString());
    }

    public static GraniteMenuTemplate apply(GraniteMenuTemplate base, String overlayYaml, String sourceLabel) {
        Object root = new Yaml().load(overlayYaml);
        if (!(root instanceof Map<?, ?> raw)) {
            throw new GraniteTemplateValidationException(List.of("Overlay root must be a YAML mapping"));
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> overlay = (Map<String, Object>) raw;
        List<String> errors = new ArrayList<>();

        String id = overlay.containsKey("id")
                ? requiredText(overlay.get("id"), "id", errors)
                : base.id();
        String title = overlay.containsKey("title")
                ? requiredText(overlay.get("title"), "title", errors)
                : base.title();
        int rows = overlay.containsKey("rows")
                ? requiredRows(overlay.get("rows"), errors)
                : base.rows();
        Optional<GraniteTemplateFill> fill = mergeFill(base.fill(), overlay, errors);
        Map<String, GraniteTemplateItem> items = mergeItems(base.items(), overlay, errors);

        if (rows >= 1 && rows <= 6) {
            int maxSlot = rows * 9 - 1;
            for (Map.Entry<String, GraniteTemplateItem> entry : items.entrySet()) {
                for (int slot : entry.getValue().slots()) {
                    if (slot < 0 || slot > maxSlot) {
                        errors.add("items." + entry.getKey() + ": slot " + slot
                                + " out of bounds for rows=" + rows + " (valid 0.." + maxSlot + ")");
                    }
                }
            }
        }
        if (!errors.isEmpty() || id == null || title == null || rows < 1) {
            throw new GraniteTemplateValidationException(errors.isEmpty()
                    ? List.of("Overlay is not a valid menu")
                    : errors);
        }
        String baseLabel = base.sourceLabel().orElse("default");
        return new GraniteMenuTemplate(
                id,
                title,
                rows,
                fill,
                items,
                Optional.of(baseLabel + " overlay " + sourceLabel));
    }

    private static Optional<GraniteTemplateFill> mergeFill(
            Optional<GraniteTemplateFill> base, Map<String, Object> overlay, List<String> errors) {
        if (!overlay.containsKey("fill")) {
            return base;
        }
        if (!(overlay.get("fill") instanceof Map<?, ?> raw)) {
            errors.add("'fill' must be a mapping");
            return base;
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> fill = (Map<String, Object>) raw;
        String material = fill.containsKey("material")
                ? requiredText(fill.get("material"), "fill.material", errors)
                : base.map(GraniteTemplateFill::material).orElse(null);
        Optional<String> name = fill.containsKey("name")
                ? optionalText(fill.get("name"), "fill.name", errors)
                : base.flatMap(GraniteTemplateFill::displayName);
        if (material == null) {
            errors.add("fill.material is required when the default menu has no fill");
            return base;
        }
        return Optional.of(new GraniteTemplateFill(material, name));
    }

    private static Map<String, GraniteTemplateItem> mergeItems(
            Map<String, GraniteTemplateItem> base, Map<String, Object> overlay, List<String> errors) {
        Map<String, GraniteTemplateItem> merged = new LinkedHashMap<>(base);
        if (!overlay.containsKey("items")) {
            return merged;
        }
        if (!(overlay.get("items") instanceof Map<?, ?> raw)) {
            errors.add("'items' must be a mapping");
            return merged;
        }
        for (Map.Entry<?, ?> entry : raw.entrySet()) {
            String key = String.valueOf(entry.getKey());
            if (!(entry.getValue() instanceof Map<?, ?> itemRaw)) {
                errors.add("items." + key + " must be a mapping");
                continue;
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> item = (Map<String, Object>) itemRaw;
            GraniteTemplateItem existing = base.get(key);
            GraniteTemplateItem next = existing == null
                    ? newItem(key, item, errors)
                    : mergeItem(key, existing, item, errors);
            if (next != null) {
                merged.put(key, next);
            }
        }
        return merged;
    }

    @Nullable
    private static GraniteTemplateItem newItem(String key, Map<String, Object> item, List<String> errors) {
        String material = requiredText(item.get("material"), "items." + key + ".material", errors);
        List<Integer> slots = slots(item, "items." + key, errors);
        if (material == null || slots == null) {
            return null;
        }
        try {
            return new GraniteTemplateItem(
                    slots,
                    material,
                    name(item, "items." + key, errors),
                    lore(item.get("lore"), "items." + key + ".lore", errors),
                    positive(item.get("amount"), "items." + key + ".amount", errors),
                    nonNegative(item.get("custom_model_data"), "items." + key + ".custom_model_data", errors),
                    item.containsKey("action") ? optionalText(item.get("action"), "items." + key + ".action", errors) : Optional.empty());
        } catch (IllegalArgumentException ex) {
            errors.add("items." + key + ": " + ex.getMessage());
            return null;
        }
    }

    @Nullable
    private static GraniteTemplateItem mergeItem(
            String key, GraniteTemplateItem base, Map<String, Object> item, List<String> errors) {
        String material = item.containsKey("material")
                ? requiredText(item.get("material"), "items." + key + ".material", errors)
                : base.material();
        List<Integer> slots = item.containsKey("slot") || item.containsKey("slots")
                ? slots(item, "items." + key, errors)
                : base.slots();
        if (material == null || slots == null) {
            return null;
        }
        Optional<String> display = item.containsKey("name") || item.containsKey("display_name")
                ? name(item, "items." + key, errors)
                : base.displayName();
        List<String> lines = item.containsKey("lore")
                ? lore(item.get("lore"), "items." + key + ".lore", errors)
                : base.lore();
        OptionalInt amount = item.containsKey("amount")
                ? positive(item.get("amount"), "items." + key + ".amount", errors)
                : base.amount();
        OptionalInt model = item.containsKey("custom_model_data")
                ? nonNegative(item.get("custom_model_data"), "items." + key + ".custom_model_data", errors)
                : base.customModelData();
        Optional<String> action = item.containsKey("action")
                ? optionalText(item.get("action"), "items." + key + ".action", errors)
                : base.action();
        try {
            return new GraniteTemplateItem(slots, material, display, lines, amount, model, action);
        } catch (IllegalArgumentException ex) {
            errors.add("items." + key + ": " + ex.getMessage());
            return null;
        }
    }

    private static Optional<String> name(Map<String, Object> item, String path, List<String> errors) {
        if (item.containsKey("name")) {
            return optionalText(item.get("name"), path + ".name", errors);
        }
        if (item.containsKey("display_name")) {
            return optionalText(item.get("display_name"), path + ".display_name", errors);
        }
        return Optional.empty();
    }

    @Nullable
    private static List<Integer> slots(Map<String, Object> item, String path, List<String> errors) {
        Object value = item.containsKey("slots") ? item.get("slots") : item.get("slot");
        if (value == null) {
            errors.add(path + " requires 'slots' or 'slot'");
            return null;
        }
        if (value instanceof List<?> list) {
            List<Integer> slots = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                Integer slot = integer(list.get(i), path + ".slots[" + i + "]", errors);
                if (slot != null) {
                    slots.add(slot);
                }
            }
            if (slots.isEmpty()) {
                errors.add(path + ".slots must not be empty");
                return null;
            }
            return slots;
        }
        Integer slot = integer(value, path + ".slot", errors);
        return slot == null ? null : List.of(slot);
    }

    private static List<String> lore(@Nullable Object value, String path, List<String> errors) {
        if (value == null) {
            return List.of();
        }
        if (value instanceof List<?> list) {
            List<String> lines = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i) == null) {
                    errors.add(path + "[" + i + "] must not be null");
                } else {
                    lines.add(String.valueOf(list.get(i)));
                }
            }
            return lines;
        }
        return List.of(String.valueOf(value));
    }

    @Nullable
    private static String requiredText(@Nullable Object value, String path, List<String> errors) {
        if (value == null) {
            errors.add("'" + path + "' must be non-blank");
            return null;
        }
        String text = String.valueOf(value).trim();
        if (text.isEmpty()) {
            errors.add("'" + path + "' must be non-blank");
            return null;
        }
        return text;
    }

    private static Optional<String> optionalText(@Nullable Object value, String path, List<String> errors) {
        String text = requiredText(value, path, errors);
        return text == null ? Optional.empty() : Optional.of(text);
    }

    private static int requiredRows(@Nullable Object value, List<String> errors) {
        Integer rows = integer(value, "rows", errors);
        if (rows == null) {
            return -1;
        }
        if (rows < 1 || rows > 6) {
            errors.add("'rows' must be between 1 and 6, got " + rows);
            return -1;
        }
        return rows;
    }

    private static OptionalInt positive(@Nullable Object value, String path, List<String> errors) {
        if (value == null) {
            return OptionalInt.empty();
        }
        Integer number = integer(value, path, errors);
        if (number == null) {
            return OptionalInt.empty();
        }
        if (number < 1) {
            errors.add(path + " must be >= 1 when set");
            return OptionalInt.empty();
        }
        return OptionalInt.of(number);
    }

    private static OptionalInt nonNegative(@Nullable Object value, String path, List<String> errors) {
        if (value == null) {
            return OptionalInt.empty();
        }
        Integer number = integer(value, path, errors);
        if (number == null) {
            return OptionalInt.empty();
        }
        if (number < 0) {
            errors.add(path + " must be >= 0 when set");
            return OptionalInt.empty();
        }
        return OptionalInt.of(number);
    }

    @Nullable
    private static Integer integer(@Nullable Object value, String path, List<String> errors) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String text) {
            try {
                return Integer.parseInt(text.trim());
            } catch (NumberFormatException ex) {
                errors.add(path + " must be an integer");
                return null;
            }
        }
        errors.add(path + " must be a number");
        return null;
    }
}
