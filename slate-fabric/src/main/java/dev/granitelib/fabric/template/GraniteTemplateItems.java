package dev.granitelib.fabric.template;

import dev.granitelib.fabric.template.model.GraniteMenuTemplate;
import dev.granitelib.fabric.template.model.GraniteTemplateItem;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Gameplay-agnostic lookups on v0 {@link GraniteMenuTemplate} items (Sprint 99).
 */
public final class GraniteTemplateItems {

    private GraniteTemplateItems() {
    }

    /**
     * Sprint 112: first template item (iteration order of {@link GraniteMenuTemplate#items()}) whose {@code action}
     * matches {@code action} after {@link GraniteTemplateActionNames#normalize(String)}.
     */
    public static Optional<GraniteTemplateItem> findFirstByAction(GraniteMenuTemplate template, String action) {
        String want = GraniteTemplateActionNames.normalize(action);
        if (want == null) {
            return Optional.empty();
        }
        for (GraniteTemplateItem item : template.items().values()) {
            Optional<String> n = item.action().map(GraniteTemplateActionNames::normalize);
            if (n.isPresent() && want.equals(n.get())) {
                return Optional.of(item);
            }
        }
        return Optional.empty();
    }

    /**
     * Sprint 112: all items whose {@code action} matches {@code action} after
     * {@link GraniteTemplateActionNames#normalize(String)} (map iteration order).
     */
    public static List<GraniteTemplateItem> findAllByAction(GraniteMenuTemplate template, String action) {
        String want = GraniteTemplateActionNames.normalize(action);
        if (want == null) {
            return List.of();
        }
        List<GraniteTemplateItem> out = new ArrayList<>();
        for (GraniteTemplateItem item : template.items().values()) {
            Optional<String> n = item.action().map(GraniteTemplateActionNames::normalize);
            if (n.isPresent() && want.equals(n.get())) {
                out.add(item);
            }
        }
        return List.copyOf(out);
    }

    /**
     * Prefer {@code template.items().get(preferredKey)}; otherwise the first item whose {@code action} equals one of
     * {@code actionAliases} (case-insensitive).
     */
    public static Optional<Integer> firstSlotByPreferredKeyOrActions(
            GraniteMenuTemplate template, String preferredKey, List<String> actionAliases) {
        GraniteTemplateItem byKey = template.items().get(preferredKey);
        if (byKey != null && !byKey.slots().isEmpty()) {
            return Optional.of(byKey.slots().getFirst());
        }
        for (GraniteTemplateItem item : template.items().values()) {
            for (String a : actionAliases) {
                if (item.action().filter(x -> x != null && a.equalsIgnoreCase(x.trim())).isPresent()
                        && !item.slots().isEmpty()) {
                    return Optional.of(item.slots().getFirst());
                }
            }
        }
        return Optional.empty();
    }

    public static boolean actionEquals(GraniteTemplateItem item, String expectedAction) {
        return item.action().map(a -> expectedAction.equalsIgnoreCase(a.trim())).orElse(false);
    }

    /**
     * Sources grid definition: YAML key {@code source}, else first item with {@link GraniteTemplateActionNames#SOURCE_ROW}.
     */
    public static @Nullable GraniteTemplateItem findSourceGridDefinition(GraniteMenuTemplate template) {
        GraniteTemplateItem byKey = template.items().get("source");
        if (byKey != null) {
            return byKey;
        }
        return findFirstByAction(template, GraniteTemplateActionNames.SOURCE_ROW).orElse(null);
    }

    /** Level grid: YAML key {@code level}, else first item with {@link GraniteTemplateActionNames#LEVEL_ROW}. */
    public static @Nullable GraniteTemplateItem findLevelGridDefinition(GraniteMenuTemplate template) {
        GraniteTemplateItem byKey = template.items().get("level");
        if (byKey != null) {
            return byKey;
        }
        return findFirstByAction(template, GraniteTemplateActionNames.LEVEL_ROW).orElse(null);
    }

    /** Skills list grid: YAML key {@code skill}, else first item with {@link GraniteTemplateActionNames#SKILL_ROW}. */
    public static @Nullable GraniteTemplateItem findSkillGridDefinition(GraniteMenuTemplate template) {
        GraniteTemplateItem byKey = template.items().get("skill");
        if (byKey != null) {
            return byKey;
        }
        return findFirstByAction(template, GraniteTemplateActionNames.SKILL_ROW).orElse(null);
    }

    /** Stats list grid: YAML key {@code stat}, else first item with {@link GraniteTemplateActionNames#STAT_ROW}. */
    public static @Nullable GraniteTemplateItem findStatGridDefinition(GraniteMenuTemplate template) {
        GraniteTemplateItem byKey = template.items().get("stat");
        if (byKey != null) {
            return byKey;
        }
        return findFirstByAction(template, GraniteTemplateActionNames.STAT_ROW).orElse(null);
    }

    /**
     * Passive abilities grid: YAML key {@code ability}, else first item with
     * {@link GraniteTemplateActionNames#ABILITY_ROW}.
     */
    public static @Nullable GraniteTemplateItem findAbilityGridDefinition(GraniteMenuTemplate template) {
        GraniteTemplateItem byKey = template.items().get("ability");
        if (byKey != null) {
            return byKey;
        }
        return findFirstByAction(template, GraniteTemplateActionNames.ABILITY_ROW).orElse(null);
    }

    /**
     * Mana ability item: YAML key {@code mana_ability}, else first item with
     * {@link GraniteTemplateActionNames#MANA_ABILITY_ROW}.
     */
    public static @Nullable GraniteTemplateItem findManaAbilityItemDefinition(GraniteMenuTemplate template) {
        GraniteTemplateItem byKey = template.items().get("mana_ability");
        if (byKey != null) {
            return byKey;
        }
        return findFirstByAction(template, GraniteTemplateActionNames.MANA_ABILITY_ROW).orElse(null);
    }

    /**
     * Leaderboard entry grid: YAML key {@code entry}, else first item with
     * {@link GraniteTemplateActionNames#LEADERBOARD_ROW}.
     */
    public static @Nullable GraniteTemplateItem findLeaderboardEntryDefinition(GraniteMenuTemplate template) {
        GraniteTemplateItem byKey = template.items().get("entry");
        if (byKey != null) {
            return byKey;
        }
        return findFirstByAction(template, GraniteTemplateActionNames.LEADERBOARD_ROW).orElse(null);
    }

    /**
     * Unclaimed items grid: YAML key {@code claimable}, else first item with
     * {@link GraniteTemplateActionNames#CLAIM_ITEM}.
     */
    public static @Nullable GraniteTemplateItem findClaimableGridDefinition(GraniteMenuTemplate template) {
        GraniteTemplateItem byKey = template.items().get("claimable");
        if (byKey != null) {
            return byKey;
        }
        return findFirstByAction(template, GraniteTemplateActionNames.CLAIM_ITEM).orElse(null);
    }
}