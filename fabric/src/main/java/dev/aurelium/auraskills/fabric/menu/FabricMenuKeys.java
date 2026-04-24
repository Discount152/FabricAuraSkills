package dev.aurelium.auraskills.fabric.menu;

/**
 * Bukkit default menu file names (subset of {@code MenuFileManager.MENU_NAMES}) used as stable IDs for
 * the Fabric registry. Future menus add keys here and register in {@link FabricMenuLoaders}.
 */
public final class FabricMenuKeys {

    /** Bukkit default {@code menus/skills.yml} */
    public static final String SKILLS = "skills";
    /** Bukkit default {@code menus/stats.yml} */
    public static final String STATS = "stats";

    private FabricMenuKeys() {
    }
}
