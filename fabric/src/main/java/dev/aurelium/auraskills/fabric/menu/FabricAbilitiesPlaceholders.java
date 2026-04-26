package dev.aurelium.auraskills.fabric.menu;

import dev.granitelib.fabric.template.GraniteTemplatePlaceholders;

import java.util.Map;

/** Placeholders for bundled {@code abilities.yml} (Sprint 102). */
public final class FabricAbilitiesPlaceholders {

    private FabricAbilitiesPlaceholders() {
    }

    public static String expand(String raw, Map<String, String> values) {
        return GraniteTemplatePlaceholders.expandAllKeysLongestFirst(raw, values);
    }

    public static boolean hasUnresolvedBraceSyntax(String str) {
        return GraniteTemplatePlaceholders.hasUnresolvedBraceSyntax(str);
    }
}
