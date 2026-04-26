package dev.aurelium.auraskills.fabric.menu;

import dev.granitelib.fabric.template.GraniteTemplatePlaceholders;

import java.util.List;
import java.util.Map;

/**
 * Minimal placeholders for template-backed {@code stat_info}. Expansion keys are explicit so missing optional fields
 * (e.g. description) substitute as empty without leaving raw braces.
 */
public final class FabricStatInfoPlaceholders {

    /** Longest keys first: {@code stat_display} / {@code stat_value} before {@code stat}. */
    private static final List<String> KEY_ORDER =
            List.of("stat_description", "stat_display", "stat_value", "stat");

    private FabricStatInfoPlaceholders() {
    }

    public static String expand(String raw, Map<String, String> values) {
        return GraniteTemplatePlaceholders.expandKeys(raw, values, KEY_ORDER);
    }

    public static boolean hasUnresolvedBraceSyntax(String s) {
        return GraniteTemplatePlaceholders.hasUnresolvedBraceSyntax(s);
    }
}
