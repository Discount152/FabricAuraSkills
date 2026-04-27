package dev.aurelium.auraskills.fabric.menu;

import dev.granitelib.fabric.template.GraniteTemplatePlaceholders;

import java.util.Map;

/** Placeholders for bundled {@code leaderboard.yml} (Sprint 103). */
public final class FabricLeaderboardPlaceholders {

    private FabricLeaderboardPlaceholders() {}

    public static String expand(String raw, Map<String, String> values) {
        return GraniteTemplatePlaceholders.expandAllKeysLongestFirst(raw, values);
    }

    public static boolean hasUnresolvedBraceSyntax(String str) {
        return GraniteTemplatePlaceholders.hasUnresolvedBraceSyntax(str);
    }
}
