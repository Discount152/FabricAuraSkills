package dev.aurelium.auraskills.fabric.menu;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Minimal local placeholder expansion for template-backed {@code sources} (Sprint 98). Not a global engine.
 */
public final class FabricSourcesPlaceholders {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{[^}]+\\}");

    private FabricSourcesPlaceholders() {
    }

    /**
     * Expands {@code {skill_display}}, {@code {skill}}, source row keys, {@code {sort}}, {@code {page}},
     * {@code {total_pages}} using {@code values} (keys without braces). Longer keys first to avoid partial
     * replacement.
     */
    public static String expand(String raw, Map<String, String> values) {
        if (raw == null) {
            return "";
        }
        String s = raw;
        s = s.replace("{total_pages}", nz(values, "total_pages"));
        s = s.replace("{skill_display}", nz(values, "skill_display"));
        s = s.replace("{source_name}", nz(values, "source_name"));
        s = s.replace("{source_type}", nz(values, "source_type"));
        s = s.replace("{source_xp}", nz(values, "source_xp"));
        s = s.replace("{skill}", nz(values, "skill"));
        s = s.replace("{sort}", nz(values, "sort"));
        s = s.replace("{page}", nz(values, "page"));
        return s;
    }

    /** True if any {@code {...}} remains after expansion (unknown or empty keys). */
    public static boolean hasUnresolvedBraceSyntax(String str) {
        return str != null && PLACEHOLDER.matcher(str).find();
    }

    private static String nz(Map<String, String> values, String key) {
        String v = values.get(key);
        return v != null ? v : "";
    }
}
