package dev.granitelib.fabric.template;

import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Documented v0 action string contract for bundled GraniteLib YAML templates (Sprint 99). Values are wired in
 * Fabric gameplay code -- this is not a dynamic action engine, only stable identifiers for authors and reviewers.
 *
 * <p>Bundled templates today: {@code stat_info.yml}, {@code sources.yml}, {@code level_progression.yml},
 * {@code skills.yml}, {@code stats.yml}, {@code abilities.yml}, {@code leaderboard.yml}, {@code unclaimed_items.yml}.
 *
 * <p><b>Sprint 112:</b> {@link #normalize(String)}, {@link #isKnown(String)}, and {@link #allKnownNormalized()} define
 * the canonical trimmed {@link Locale#ROOT} lowercase vocabulary for template validation and item lookups.
 * Sprint 113 adds navigation actions for the locked skills/stats hub ({@code open_leaderboard}, {@code open_stats},
 * {@code open_unclaimed_items}, {@code close}); {@link #allKnownNormalized()} size is <strong>21</strong>.
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

    /** One claimable unclaimed reward slot (Fabric unclaimed_items). */
    public static final String CLAIM_ITEM = "claim_item";

    /** Merge all claimable items (Fabric unclaimed_items). */
    public static final String CLAIM_ALL = "claim_all";

    /** Open leaderboard browser from skills menu (Fabric). */
    public static final String OPEN_LEADERBOARD = "open_leaderboard";

    /** Open stats list from skills menu (Fabric). */
    public static final String OPEN_STATS = "open_stats";

    /** Open unclaimed reward list (Fabric skills menu utility). */
    public static final String OPEN_UNCLAIMED_ITEMS = "open_unclaimed_items";

    /** Close the current screen (Fabric top-level menus). */
    public static final String CLOSE = "close";

    /** Open per-skill sources menu (Fabric level_progression). */
    public static final String OPEN_SOURCES = "open_sources";

    /** Open per-skill abilities menu (Fabric level_progression). */
    public static final String OPEN_ABILITIES = "open_abilities";

    public static final String PREVIOUS_PAGE = "previous_page";

    public static final String NEXT_PAGE = "next_page";

    public static final String CYCLE_SORT = "cycle_sort";

    private static final Set<String> ALL_KNOWN_NORMALIZED = Set.copyOf(List.of(
            BACK,
            STAT_PANEL,
            SOURCE_ROW,
            LEVEL_ROW,
            SKILL_ROW,
            STAT_ROW,
            ABILITY_ROW,
            MANA_ABILITY_ROW,
            LEADERBOARD_ROW,
            REFRESH,
            CLAIM_ITEM,
            CLAIM_ALL,
            OPEN_LEADERBOARD,
            OPEN_STATS,
            OPEN_UNCLAIMED_ITEMS,
            CLOSE,
            OPEN_SOURCES,
            OPEN_ABILITIES,
            PREVIOUS_PAGE,
            NEXT_PAGE,
            CYCLE_SORT));

    /**
     * Sprint 112+113: stable normalization for action comparisons -- trim, then {@link Locale#ROOT} lowercase; {@code
     * null} or whitespace-only input yields {@code null}.
     */
    public static String normalize(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        if (t.isEmpty()) {
            return null;
        }
        return t.toLowerCase(Locale.ROOT);
    }

    /** Immutable set of all {@linkplain #normalize(String) normalized} v0 action literals. */
    public static Set<String> allKnownNormalized() {
        return ALL_KNOWN_NORMALIZED;
    }

    /** Sprint 112: whether {@code s} normalizes to a documented v0 action (see {@link #allKnownNormalized()}). */
    public static boolean isKnown(String s) {
        String n = normalize(s);
        return n != null && ALL_KNOWN_NORMALIZED.contains(n);
    }
}