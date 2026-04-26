package dev.aurelium.auraskills.fabric.menu;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Minimal local placeholder expansion for template-backed {@code stat_info} (Sprint 96). Not a global engine.
 */
public final class FabricStatInfoPlaceholders {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{[^}]+\\}");

    private FabricStatInfoPlaceholders() {
    }

    /**
     * Expands {@code {stat}}, {@code {stat_display}}, {@code {stat_description}}, {@code {stat_value}} using
     * {@code values} (keys without braces). Order avoids accidental partial replacements.
     */
    public static String expand(String raw, Map<String, String> values) {
        if (raw == null) {
            return "";
        }
        String s = raw;
        s = s.replace("{stat_value}", nz(values, "stat_value"));
        s = s.replace("{stat_description}", nz(values, "stat_description"));
        s = s.replace("{stat_display}", nz(values, "stat_display"));
        s = s.replace("{stat}", nz(values, "stat"));
        return s;
    }

    /** True if any {@code {...}} remains after expansion (unknown or empty keys). */
    public static boolean hasUnresolvedBraceSyntax(String s) {
        return s != null && PLACEHOLDER.matcher(s).find();
    }

    private static String nz(Map<String, String> values, String key) {
        String v = values.get(key);
        return v != null ? v : "";
    }
}
