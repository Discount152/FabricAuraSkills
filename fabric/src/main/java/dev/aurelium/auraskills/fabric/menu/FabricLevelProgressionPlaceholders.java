package dev.aurelium.auraskills.fabric.menu;

import dev.granitelib.fabric.template.GraniteTemplatePlaceholders;

import java.util.List;
import java.util.Map;

/** Placeholders for bundled {@code level_progression.yml} (Sprint 100). */
public final class FabricLevelProgressionPlaceholders {

    private static final List<String> KEY_ORDER =
            List.of(
                    "level_xp_required",
                    "level_status",
                    "level_detail",
                    "skill_display",
                    "current_xp",
                    "total_pages",
                    "skill",
                    "level",
                    "page");

    private FabricLevelProgressionPlaceholders() {
    }

    public static String expand(String raw, Map<String, String> values) {
        return GraniteTemplatePlaceholders.expandKeys(raw, values, KEY_ORDER);
    }

    public static boolean hasUnresolvedBraceSyntax(String s) {
        return GraniteTemplatePlaceholders.hasUnresolvedBraceSyntax(s);
    }
}
