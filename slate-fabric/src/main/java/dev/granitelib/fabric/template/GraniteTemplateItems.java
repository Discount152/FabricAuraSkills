package dev.granitelib.fabric.template;

import dev.granitelib.fabric.template.model.GraniteMenuTemplate;
import dev.granitelib.fabric.template.model.GraniteTemplateItem;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * Gameplay-agnostic lookups on v0 {@link GraniteMenuTemplate} items (Sprint 99).
 */
public final class GraniteTemplateItems {

    private GraniteTemplateItems() {
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
        for (GraniteTemplateItem item : template.items().values()) {
            if (item.action()
                    .filter(a -> GraniteTemplateActionNames.SOURCE_ROW.equalsIgnoreCase(a))
                    .isPresent()) {
                return item;
            }
        }
        return null;
    }

    /** Level grid: YAML key {@code level}, else first item with {@link GraniteTemplateActionNames#LEVEL_ROW}. */
    public static @Nullable GraniteTemplateItem findLevelGridDefinition(GraniteMenuTemplate template) {
        GraniteTemplateItem byKey = template.items().get("level");
        if (byKey != null) {
            return byKey;
        }
        for (GraniteTemplateItem item : template.items().values()) {
            if (item.action()
                    .filter(a -> GraniteTemplateActionNames.LEVEL_ROW.equalsIgnoreCase(a))
                    .isPresent()) {
                return item;
            }
        }
        return null;
    }

    /** Skills list grid: YAML key {@code skill}, else first item with {@link GraniteTemplateActionNames#SKILL_ROW}. */
    public static @Nullable GraniteTemplateItem findSkillGridDefinition(GraniteMenuTemplate template) {
        GraniteTemplateItem byKey = template.items().get("skill");
        if (byKey != null) {
            return byKey;
        }
        for (GraniteTemplateItem item : template.items().values()) {
            if (item.action()
                    .filter(a -> GraniteTemplateActionNames.SKILL_ROW.equalsIgnoreCase(a))
                    .isPresent()) {
                return item;
            }
        }
        return null;
    }

    /** Stats list grid: YAML key {@code stat}, else first item with {@link GraniteTemplateActionNames#STAT_ROW}. */
    public static @Nullable GraniteTemplateItem findStatGridDefinition(GraniteMenuTemplate template) {
        GraniteTemplateItem byKey = template.items().get("stat");
        if (byKey != null) {
            return byKey;
        }
        for (GraniteTemplateItem item : template.items().values()) {
            if (item.action()
                    .filter(a -> GraniteTemplateActionNames.STAT_ROW.equalsIgnoreCase(a))
                    .isPresent()) {
                return item;
            }
        }
        return null;
    }
}
