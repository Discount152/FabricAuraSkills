package dev.aurelium.auraskills.fabric.menu;

import dev.granitelib.fabric.template.GraniteTemplatePlaceholders;

import java.util.List;
import java.util.Map;

/**
 * Minimal placeholders for template-backed {@code sources}. Keys are ordered longest-first for safe substitution.
 */
public final class FabricSourcesPlaceholders {

    private static final List<String> KEY_ORDER =
            List.of(
                    "skill_display",
                    "total_pages",
                    "source_name",
                    "source_type",
                    "source_xp",
                    "skill",
                    "sort",
                    "page");

    private FabricSourcesPlaceholders() {
    }

    public static String expand(String raw, Map<String, String> values) {
        return GraniteTemplatePlaceholders.expandKeys(raw, values, KEY_ORDER);
    }

    public static boolean hasUnresolvedBraceSyntax(String str) {
        return GraniteTemplatePlaceholders.hasUnresolvedBraceSyntax(str);
    }
}
