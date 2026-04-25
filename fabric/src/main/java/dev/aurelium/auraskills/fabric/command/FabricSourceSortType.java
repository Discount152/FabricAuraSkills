package dev.aurelium.auraskills.fabric.command;

import dev.aurelium.auraskills.api.source.XpSource;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.Locale;
import java.util.Optional;

/**
 * Matches Bukkit {@code SourcesMenu.SortType} / {@code @sort_types} (lowercase names in the
 * reference ACF completer) for {@code /sources} text output (**Sprint 57**).
 */
public enum FabricSourceSortType {
    ASCENDING,
    DESCENDING,
    ALPHABETICAL,
    REVERSE_ALPHABETICAL;

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

    public Comparator<XpSource> comparator(Locale locale) {
        return switch (this) {
            case ASCENDING -> Comparator.comparingDouble(XpSource::getXp);
            case DESCENDING -> Comparator.comparingDouble(XpSource::getXp).reversed();
            case ALPHABETICAL ->
                    Comparator.comparing(s -> s.getDisplayName(locale), String::compareTo);
            case REVERSE_ALPHABETICAL ->
                    Comparator.comparing(
                            s -> s.getDisplayName(locale), (a, b) -> b.compareTo(a));
        };
    }
}
