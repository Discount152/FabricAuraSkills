package dev.aurelium.auraskills.fabric.command;

import dev.aurelium.auraskills.api.source.XpSource;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.menu.FabricXpSourceMenuDisplay;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;

/**
 * Matches Bukkit {@code SourcesMenu.SortType} / {@code @sort_types} (lowercase names in the
 * reference ACF completer) for {@code /sources} text and the sources menu (**Sprint 57**, **Sprint
 * 69**).
 */
public enum FabricSourceSortType {
    ASCENDING,
    DESCENDING,
    ALPHABETICAL,
    REVERSE_ALPHABETICAL;

    /** Cycles in the same order as Bukkit’s {@code SourcesMenu} sorter. */
    public FabricSourceSortType nextCyclic() {
        return values()[(ordinal() + 1) % values().length];
    }

    public static Optional<FabricSourceSortType> fromTokenStrict(@Nullable String raw) {
        if (raw == null || raw.isEmpty()) {
            return Optional.of(ASCENDING);
        }
        return switch (raw.toLowerCase(Locale.ROOT)) {
            case "ascending" -> Optional.of(ASCENDING);
            case "descending" -> Optional.of(DESCENDING);
            case "alphabetical" -> Optional.of(ALPHABETICAL);
            case "reverse_alphabetical" -> Optional.of(REVERSE_ALPHABETICAL);
            default -> Optional.empty();
        };
    }

    public Comparator<XpSource> comparator(Locale locale, FabricAuraSkills plugin) {
        Function<XpSource, String> alphabeticalKey =
                s -> FabricXpSourceMenuDisplay.effectiveRowTitle(plugin, s, locale);
        return comparator(locale, alphabeticalKey);
    }

    Comparator<XpSource> comparator(Locale locale, Function<XpSource, String> alphabeticalKey) {
        return switch (this) {
            case ASCENDING -> Comparator.comparingDouble(XpSource::getXp);
            case DESCENDING -> Comparator.comparingDouble(XpSource::getXp).reversed();
            case ALPHABETICAL ->
                    Comparator.comparing(alphabeticalKey, Comparator.nullsLast(String::compareTo));
            case REVERSE_ALPHABETICAL ->
                    Comparator.comparing(alphabeticalKey, Comparator.nullsLast(String::compareTo))
                            .reversed();
        };
    }
}
