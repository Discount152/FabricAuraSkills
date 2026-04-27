package dev.aurelium.auraskills.fabric.menu;

import dev.granitelib.fabric.template.GraniteTemplateActionNames;
import dev.granitelib.fabric.template.GraniteTemplateItems;
import dev.granitelib.fabric.template.model.GraniteMenuTemplate;
import dev.granitelib.fabric.template.model.GraniteTemplateItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Sprint 112: per-menu allow-list and required-action contract for Granite template {@code action} strings (normalized
 * with {@link GraniteTemplateActionNames#normalize(String)}). Unknown menu ids yield no validation
 * ({@link Optional#empty()}).
 */
public final class FabricGraniteMenuTemplateActionContract {

    private static final Set<String> NAVIGATION_NORMALIZED = Set.copyOf(List.of(
            GraniteTemplateActionNames.BACK,
            GraniteTemplateActionNames.PREVIOUS_PAGE,
            GraniteTemplateActionNames.NEXT_PAGE,
            GraniteTemplateActionNames.OPEN_SOURCES,
            GraniteTemplateActionNames.OPEN_ABILITIES,
            GraniteTemplateActionNames.CYCLE_SORT,
            GraniteTemplateActionNames.REFRESH,
            GraniteTemplateActionNames.CLAIM_ALL));

    private static final Set<String> ROW_OR_PANEL_NORMALIZED = Set.copyOf(List.of(
            GraniteTemplateActionNames.SOURCE_ROW,
            GraniteTemplateActionNames.LEVEL_ROW,
            GraniteTemplateActionNames.SKILL_ROW,
            GraniteTemplateActionNames.STAT_ROW,
            GraniteTemplateActionNames.ABILITY_ROW,
            GraniteTemplateActionNames.MANA_ABILITY_ROW,
            GraniteTemplateActionNames.LEADERBOARD_ROW,
            GraniteTemplateActionNames.CLAIM_ITEM,
            GraniteTemplateActionNames.STAT_PANEL));

    private static final Map<String, Set<String>> ALLOWED_NORMALIZED_BY_MENU_ID;

    static {
        Map<String, Set<String>> m = new HashMap<>();
        m.put(
                "stat_info",
                Set.copyOf(
                        List.of(GraniteTemplateActionNames.STAT_PANEL, GraniteTemplateActionNames.BACK)));
        m.put(
                "sources",
                Set.copyOf(
                        List.of(
                                GraniteTemplateActionNames.SOURCE_ROW,
                                GraniteTemplateActionNames.BACK,
                                GraniteTemplateActionNames.PREVIOUS_PAGE,
                                GraniteTemplateActionNames.CYCLE_SORT,
                                GraniteTemplateActionNames.NEXT_PAGE)));
        m.put(
                "level_progression",
                Set.copyOf(
                        List.of(
                                GraniteTemplateActionNames.LEVEL_ROW,
                                GraniteTemplateActionNames.OPEN_SOURCES,
                                GraniteTemplateActionNames.OPEN_ABILITIES,
                                GraniteTemplateActionNames.BACK,
                                GraniteTemplateActionNames.PREVIOUS_PAGE,
                                GraniteTemplateActionNames.NEXT_PAGE)));
        m.put(
                "skills",
                Set.copyOf(
                        List.of(
                                GraniteTemplateActionNames.SKILL_ROW,
                                GraniteTemplateActionNames.PREVIOUS_PAGE,
                                GraniteTemplateActionNames.NEXT_PAGE)));
        m.put(
                "stats",
                Set.copyOf(
                        List.of(
                                GraniteTemplateActionNames.STAT_ROW,
                                GraniteTemplateActionNames.PREVIOUS_PAGE,
                                GraniteTemplateActionNames.NEXT_PAGE)));
        m.put(
                "abilities",
                Set.copyOf(
                        List.of(
                                GraniteTemplateActionNames.ABILITY_ROW,
                                GraniteTemplateActionNames.MANA_ABILITY_ROW,
                                GraniteTemplateActionNames.BACK)));
        m.put(
                "leaderboard",
                Set.copyOf(
                        List.of(
                                GraniteTemplateActionNames.LEADERBOARD_ROW,
                                GraniteTemplateActionNames.BACK,
                                GraniteTemplateActionNames.PREVIOUS_PAGE,
                                GraniteTemplateActionNames.REFRESH,
                                GraniteTemplateActionNames.NEXT_PAGE)));
        m.put(
                "unclaimed_items",
                Set.copyOf(
                        List.of(
                                GraniteTemplateActionNames.CLAIM_ITEM,
                                GraniteTemplateActionNames.CLAIM_ALL,
                                GraniteTemplateActionNames.PREVIOUS_PAGE,
                                GraniteTemplateActionNames.NEXT_PAGE)));
        ALLOWED_NORMALIZED_BY_MENU_ID = Map.copyOf(m);
    }

    private FabricGraniteMenuTemplateActionContract() {
    }

    /** Normalized action is a navigation/control action for template authoring helpers. */
    public static boolean isNavigationNormalized(String normalized) {
        return normalized != null && NAVIGATION_NORMALIZED.contains(normalized);
    }

    /** Normalized action is a row or panel grid action for template authoring helpers. */
    public static boolean isRowOrPanelNormalized(String normalized) {
        return normalized != null && ROW_OR_PANEL_NORMALIZED.contains(normalized);
    }

    /**
     * Validates that every non-empty item action is known and allowed for {@code menuId}, and that each action in the
     * menu contract appears on at least one item. Returns {@link Optional#empty()} when {@code menuId} has no contract
     * or when validation passes.
     */
    public static Optional<String> validateTemplateActions(String menuId, GraniteMenuTemplate template) {
        Set<String> allowed = ALLOWED_NORMALIZED_BY_MENU_ID.get(menuId);
        if (allowed == null) {
            return Optional.empty();
        }
        for (GraniteTemplateItem item : template.items().values()) {
            Optional<String> actOpt = item.action();
            if (actOpt.isEmpty()) {
                continue;
            }
            String raw = actOpt.get();
            if (raw.trim().isEmpty()) {
                continue;
            }
            if (!GraniteTemplateActionNames.isKnown(raw)) {
                return Optional.of("unknown action '" + raw + "'");
            }
            String n = GraniteTemplateActionNames.normalize(raw);
            if (n == null) {
                return Optional.of("invalid action '" + raw + "'");
            }
            if (!allowed.contains(n)) {
                return Optional.of("action '" + n + "' is not allowed for menu '" + menuId + "'");
            }
        }
        for (String required : allowed) {
            if (GraniteTemplateItems.findAllByAction(template, required).isEmpty()) {
                return Optional.of("required action '" + required + "' is missing");
            }
        }
        return Optional.empty();
    }
}