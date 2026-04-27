package dev.granitelib.fabric.template;

/**
 * Documented v0 action string contract for bundled GraniteLib YAML templates (Sprint 99). Values are wired in
 * Fabric gameplay code — this is not a dynamic action engine, only stable identifiers for authors and reviewers.
 *
 * <p>Bundled templates today: {@code stat_info.yml}, {@code sources.yml}, {@code level_progression.yml},
 * {@code skills.yml}, {@code stats.yml}, {@code abilities.yml}, {@code leaderboard.yml}.
 */
public final class GraniteTemplateActionNames {

    private GraniteTemplateActionNames() {
    }

    /** Return to the previous menu (e.g. stats or level progression). */
    public static final String BACK = "back";

    /** Stat detail panel item; may receive generated append lore (Fabric stat_info). */
    public static final String STAT_PANEL = "stat_panel";

    /** One row in the sources XP grid (Fabric sources). */
    public static final String SOURCE_ROW = "source_row";

    /** One cell in the level progression grid (Fabric level_progression). */
    public static final String LEVEL_ROW = "level_row";

    /** One row in the skills list grid (Fabric skills). */
    public static final String SKILL_ROW = "skill_row";

    /** One row in the stats list grid (Fabric stats). */
    public static final String STAT_ROW = "stat_row";

    /** One passive ability row (Fabric abilities). */
    public static final String ABILITY_ROW = "ability_row";

    /** Mana ability display item (Fabric abilities). */
    public static final String MANA_ABILITY_ROW = "mana_ability_row";

    /** One ranked row in the leaderboard grid (Fabric leaderboard). */
    public static final String LEADERBOARD_ROW = "leaderboard_row";

    /** Re-open current leaderboard view from cache (Fabric leaderboard). */
    public static final String REFRESH = "refresh";

    /** Open per-skill sources menu (Fabric level_progression). */
    public static final String OPEN_SOURCES = "open_sources";

    /** Open per-skill abilities menu (Fabric level_progression). */
    public static final String OPEN_ABILITIES = "open_abilities";

    public static final String PREVIOUS_PAGE = "previous_page";

    public static final String NEXT_PAGE = "next_page";

    public static final String CYCLE_SORT = "cycle_sort";
}
