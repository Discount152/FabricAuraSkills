package dev.granitelib.fabric.template.load;

import dev.granitelib.fabric.template.model.GraniteMenuTemplate;
import dev.granitelib.fabric.template.model.GraniteTemplateFill;
import dev.granitelib.fabric.template.model.GraniteTemplateItem;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.Reader;
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
 * Read-only YAML → {@link GraniteMenuTemplate}. v0 schema only; no hot reload, no gameplay wiring.
 */
public final class GraniteMenuTemplateYamlLoader {

    private GraniteMenuTemplateYamlLoader() {
    }

    public static GraniteMenuTemplate loadPath(Path path) throws IOException, GraniteTemplateValidationException {
        String text = Files.readString(path, StandardCharsets.UTF_8);
        return loadString(text, path.toString());
    }

    public static GraniteMenuTemplate loadString(String yamlText, String sourceLabel)
            throws GraniteTemplateValidationException {
        Object root = new Yaml().load(yamlText);
        return parseRoot(root, sourceLabel);
    }

    public static GraniteMenuTemplate loadReader(Reader reader, String sourceLabel) {
        try {
            Object root = new Yaml().load(reader);
            return parseRoot(root, sourceLabel);
        } catch (RuntimeException e) {
            throw new GraniteTemplateValidationException(List.of("YAML parse error: " + e.getMessage()));
        }
    }

    @SuppressWarnings("unchecked")
    private static GraniteMenuTemplate parseRoot(Object root, String sourceLabel) {
        List<String> errors = new ArrayList<>();
        if (!(root instanceof Map<?, ?> rawMap)) {
            throw new GraniteTemplateValidationException(List.of("Root must be a YAML mapping"));
        }
        Map<String, Object> map = (Map<String, Object>) rawMap;

        String id = requireNonBlankString(map, "id", errors);
        String title = requireNonBlankString(map, "title", errors);
        Integer rowsBoxed = requirePositiveIntInRange(map, "rows", 1, 6, errors);

        Optional<GraniteTemplateFill> fill = Optional.empty();
        if (map.containsKey("fill") && map.get("fill") != null) {
            GraniteTemplateFill parsed = parseFill(map.get("fill"), errors);
            if (parsed != null) {
                fill = Optional.of(parsed);
            }
        }

        Map<String, GraniteTemplateItem> items = new LinkedHashMap<>();
        if (map.containsKey("items") && map.get("items") != null) {
            Object itemsObj = map.get("items");
            if (!(itemsObj instanceof Map<?, ?>)) {
                errors.add("'items' must be a mapping");
            } else {
                for (Map.Entry<?, ?> e : ((Map<?, ?>) itemsObj).entrySet()) {
                    String key = String.valueOf(e.getKey());
                    GraniteTemplateItem item = parseItem(key, e.getValue(), errors);
                    if (item != null) {
                        items.put(key, item);
                    }
                }
            }
        }

        if (rowsBoxed != null) {
            int maxSlot = rowsBoxed * 9 - 1;
            for (Map.Entry<String, GraniteTemplateItem> e : items.entrySet()) {
                for (int slot : e.getValue().slots()) {
                    if (slot < 0 || slot > maxSlot) {
                        errors.add(
                                "items." + e.getKey() + ": slot " + slot + " out of bounds for rows="
                                        + rowsBoxed + " (valid 0.." + maxSlot + ")");
                    }
                }
            }
        }

        if (!errors.isEmpty()) {
            throw new GraniteTemplateValidationException(errors);
        }

        return new GraniteMenuTemplate(
                id,
                title,
                rowsBoxed,
                fill,
                items,
                Optional.ofNullable(sourceLabel));
    }

    @Nullable
    private static GraniteTemplateFill parseFill(Object fillObj, List<String> errors) {
        if (!(fillObj instanceof Map<?, ?> raw)) {
            errors.add("'fill' must be a mapping");
            return null;
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> m = (Map<String, Object>) raw;
        String material = requireNonBlankString(m, "material", errors);
        if (material == null) {
            return null;
        }
        Optional<String> name = optionalString(m.get("name"));
        return new GraniteTemplateFill(material, name);
    }

    @Nullable
    private static GraniteTemplateItem parseItem(String itemKey, Object value, List<String> errors) {
        if (!(value instanceof Map<?, ?> raw)) {
            errors.add("items." + itemKey + " must be a mapping");
            return null;
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> m = (Map<String, Object>) raw;
        String material = requireNonBlankString(m, "material", errors);
        List<Integer> slots = parseSlots(m, "items." + itemKey, errors);
        if (material == null || slots == null) {
            return null;
        }
        Optional<String> display = optionalString(m.get("name"));
        if (display.isEmpty()) {
            display = optionalString(m.get("display_name"));
        }
        List<String> lore = parseLore(m.get("lore"), "items." + itemKey + ".lore", errors);
        OptionalInt amount = optionalPositiveInt(m.get("amount"), "items." + itemKey + ".amount", errors);
        OptionalInt cmd = optionalNonNegativeInt(m.get("custom_model_data"), "items." + itemKey + ".custom_model_data", errors);
        Optional<String> action = optionalString(m.get("action"));
        try {
            return new GraniteTemplateItem(slots, material, display, lore, amount, cmd, action);
        } catch (IllegalArgumentException ex) {
            errors.add("items." + itemKey + ": " + ex.getMessage());
            return null;
        }
    }

    private static List<String> parseLore(@Nullable Object loreObj, String path, List<String> errors) {
        if (loreObj == null) {
            return List.of();
        }
        if (loreObj instanceof List<?> list) {
            List<String> out = new ArrayList<>();
            int i = 0;
            for (Object o : list) {
                if (o == null) {
                    errors.add(path + "[" + i + "] must not be null");
                } else {
                    out.add(String.valueOf(o));
                }
                i++;
            }
            return out;
        }
        return List.of(String.valueOf(loreObj));
    }

    @Nullable
    private static List<Integer> parseSlots(Map<String, Object> m, String path, List<String> errors) {
        if (m.containsKey("slots") && m.get("slots") != null) {
            Object slotsObj = m.get("slots");
            if (slotsObj instanceof List<?> list) {
                List<Integer> out = new ArrayList<>();
                int i = 0;
                for (Object o : list) {
                    Integer v = asInt(o, path + ".slots[" + i + "]", errors);
                    if (v != null) {
                        out.add(v);
                    }
                    i++;
                }
                if (out.isEmpty() && !list.isEmpty()) {
                    return null;
                }
                if (out.isEmpty()) {
                    errors.add(path + ".slots must not be empty");
                    return null;
                }
                return out;
            }
            Integer one = asInt(slotsObj, path + ".slots", errors);
            if (one == null) {
                return null;
            }
            return List.of(one);
        }
        if (m.containsKey("slot") && m.get("slot") != null) {
            Integer one = asInt(m.get("slot"), path + ".slot", errors);
            if (one == null) {
                return null;
            }
            return List.of(one);
        }
        errors.add(path + " requires 'slots' or 'slot'");
        return null;
    }

    @Nullable
    private static Integer asInt(Object o, String path, List<String> errors) {
        if (o instanceof Number n) {
            return n.intValue();
        }
        if (o instanceof String s) {
            try {
                return Integer.parseInt(s.trim());
            } catch (NumberFormatException ex) {
                errors.add(path + " must be an integer");
                return null;
            }
        }
        errors.add(path + " must be a number");
        return null;
    }

    @Nullable
    private static String requireNonBlankString(Map<String, Object> map, String key, List<String> errors) {
        if (!map.containsKey(key) || map.get(key) == null) {
            errors.add("missing required key '" + key + "'");
            return null;
        }
        String s = String.valueOf(map.get(key)).trim();
        if (s.isEmpty()) {
            errors.add("'" + key + "' must be non-blank");
            return null;
        }
        return s;
    }

    @Nullable
    private static Integer requirePositiveIntInRange(
            Map<String, Object> map, String key, int min, int max, List<String> errors) {
        if (!map.containsKey(key) || map.get(key) == null) {
            errors.add("missing required key '" + key + "'");
            return null;
        }
        Integer v = asInt(map.get(key), key, errors);
        if (v == null) {
            return null;
        }
        if (v < min || v > max) {
            errors.add("'" + key + "' must be between " + min + " and " + max + ", got " + v);
            return null;
        }
        return v;
    }

    private static Optional<String> optionalString(@Nullable Object o) {
        if (o == null) {
            return Optional.empty();
        }
        String s = String.valueOf(o).trim();
        return s.isEmpty() ? Optional.empty() : Optional.of(s);
    }

    private static OptionalInt optionalPositiveInt(@Nullable Object o, String path, List<String> errors) {
        if (o == null) {
            return OptionalInt.empty();
        }
        Integer v = asInt(o, path, errors);
        if (v == null) {
            return OptionalInt.empty();
        }
        if (v < 1) {
            errors.add(path + " must be >= 1 when set");
            return OptionalInt.empty();
        }
        return OptionalInt.of(v);
    }

    private static OptionalInt optionalNonNegativeInt(@Nullable Object o, String path, List<String> errors) {
        if (o == null) {
            return OptionalInt.empty();
        }
        Integer v = asInt(o, path, errors);
        if (v == null) {
            return OptionalInt.empty();
        }
        if (v < 0) {
            errors.add(path + " must be >= 0 when set");
            return OptionalInt.empty();
        }
        return OptionalInt.of(v);
    }
}
