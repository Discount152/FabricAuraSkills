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
    /**
     * Bukkit default {@code menus/level_progression.yml} — registered in {@link FabricMenuRegistry} (Sprint
     * 35+); open with {@link FabricMenuContext#ofSkill} or {@link FabricAuraSkillsMenus#openLevelProgression}.
     */
    public static final String LEVEL_PROGRESSION = "level_progression";
    /**
     * Bukkit default {@code menus/stat_info.yml} — registered in {@link FabricMenuRegistry} (Sprint 35+); open
     * with {@link FabricMenuContext#ofStat} or {@link FabricAuraSkillsMenus#openStatInfo}.
     */
    public static final String STAT_INFO = "stat_info";
    /**
     * Bukkit default {@code menus/sources.yml} — **not** registered in {@link FabricMenuLoaders} (Sprint
     * 69+); open via {@code /sources} or {@code /openmenu sources} and
     * {@link dev.aurelium.auraskills.fabric.menu.FabricAuraSkillsMenus#openSources}.
     */
    public static final String SOURCES = "sources";
    /**
     * Bukkit {@code UnclaimedItemsMenu} (Slate) — **not** loaded from {@code menus/*.yml} on Fabric;
     * use {@link FabricAuraSkillsMenus#openUnclaimedItems} (**Sprint 71**).
     */
    public static final String UNCLAIMED_ITEMS = "unclaimed_items";
    /**
     * Bukkit {@code LeaderboardMenu} (Slate) — **not** loaded from {@code menus/*.yml} on Fabric;
     * use {@link FabricAuraSkillsMenus#openLeaderboard} or {@code /openmenu leaderboard} (**Sprint 72**).
     */
    public static final String LEADERBOARD = "leaderboard";
    /**
     * Bukkit {@code AbilitiesMenu} (Slate) — **not** loaded from {@code menus/*.yml} on Fabric;
     * use {@link FabricAuraSkillsMenus#openAbilities} or {@code /openmenu abilities} (**Sprint 73**).
     */
    public static final String ABILITIES = "abilities";

    private FabricMenuKeys() {
    }
}
