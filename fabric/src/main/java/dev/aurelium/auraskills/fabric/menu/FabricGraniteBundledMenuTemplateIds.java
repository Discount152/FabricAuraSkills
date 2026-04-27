package dev.aurelium.auraskills.fabric.menu;

import java.util.List;

/**
 * Canonical list of bundled Granite v0 menu templates (Sprint 108). Keep in sync with
 * {@code assets/auraskills/granite/templates/*.yml}, {@link FabricGraniteMenuTemplateLoader} disk names, and
 * {@link FabricGraniteMenuTemplateBindings} anchors (**Sprint 110**).
 */
public final class FabricGraniteBundledMenuTemplateIds {

    /** Relative to classpath root (no leading {@code /}). */
    public static final String CLASSPATH_DIR = "assets/auraskills/granite/templates/";

    public static final List<String> ALL = List.of(
            "stat_info",
            "sources",
            "level_progression",
            "skills",
            "stats",
            "abilities",
            "leaderboard",
            "unclaimed_items");

    private FabricGraniteBundledMenuTemplateIds() {}

    public static String classpathPathFor(String menuId) {
        return CLASSPATH_DIR + menuId + ".yml";
    }
}
