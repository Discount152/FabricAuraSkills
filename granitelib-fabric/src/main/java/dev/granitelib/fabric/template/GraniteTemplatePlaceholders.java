package dev.granitelib.fabric.template;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Minimal v0 placeholder helpers for GraniteLib templates (Sprint 99+). Not a global PlaceholderAPI-style
 * engine: no nested placeholders, conditionals, or formatters.
 */
public final class GraniteTemplatePlaceholders {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{[^}]+\\}");

    private GraniteTemplatePlaceholders() {
    }

    /**
     * Replaces {@code {key}} tokens using {@code values}. Only keys listed in {@code keysInReplaceOrder} are
     * substituted; missing map entries use an empty string. Order must be safe for overlapping key names (longer
     * keys before shorter prefixes, e.g. {@code stat_display} before {@code stat}).
     */
    public static String expandKeys(String raw, Map<String, String> values, List<String> keysInReplaceOrder) {
        if (raw == null) {
            return "";
        }
        String s = raw;
        for (String k : keysInReplaceOrder) {
            String v = values.get(k);
            if (v == null) {
                v = "";
            }
            s = s.replace("{" + k + "}", v);
        }
        return s;
    }

    /**
     * Replaces every {@code {key}} where {@code key} is present in {@code values}. Keys are processed
     * longest-first (then lexicographic) to reduce accidental partial replacement when keys share prefixes.
     */
    public static String expandAllKeysLongestFirst(String raw, Map<String, String> values) {
        if (raw == null) {
            return "";
        }
        List<String> keys = new ArrayList<>(values.keySet());
        keys.sort(
                Comparator.comparingInt(String::length)
                        .reversed()
                        .thenComparing(Comparator.naturalOrder()));
        return expandKeys(raw, values, keys);
    }

    /**
     * Replaces known placeholders in a display name and lists every placeholder that had no value.
     */
    public static GranitePlaceholderResult replace(String name, Map<String, String> values) {
        String text = expandAllKeysLongestFirst(name, values == null ? Map.of() : values);
        return new GranitePlaceholderResult(text, unknown(text));
    }

    public static List<String> unknown(String text) {
        if (text == null || text.isEmpty()) {
            return List.of();
        }
        java.util.regex.Matcher matcher = PLACEHOLDER.matcher(text);
        List<String> found = new ArrayList<>();
        while (matcher.find()) {
            String token = matcher.group();
            found.add(token.substring(1, token.length() - 1));
        }
        return List.copyOf(found);
    }

    /** True if any {@code {...}} substring remains (typically unknown token after expansion). */
    public static boolean hasUnresolvedBraceSyntax(String str) {
        return str != null && PLACEHOLDER.matcher(str).find();
    }
}
