package dev.aurelium.auraskills.fabric.command;

import dev.aurelium.auraskills.fabric.menu.FabricMenuKeys;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * Whitelist + normalization for **bounded** built-in {@code /openmenu}-style access (**Sprint
 * 44+**). Ids match {@link FabricMenuKeys}; list/detail menus without required context use
 * {@link dev.aurelium.auraskills.fabric.menu.FabricMenuContext#none()}; {@code level_progression} /
 * {@code stat_info} require {@link dev.aurelium.auraskills.fabric.menu.FabricMenuContext#ofSkill}
 * / {@link dev.aurelium.auraskills.fabric.menu.FabricMenuContext#ofStat} (see
 * {@link FabricCommandBuiltInMenuId#requiresContext(String)}).
 */
public final class FabricCommandBuiltInMenuId {

    /** Menu ids allowed on the bounded {@code openmenu} command (Sprint 45+; {@code sources} Sprint 70; {@code unclaimed_items} Sprint 71). */
    public static final Set<String> OPENVIA_COMMAND =
            Set.of(
                    FabricMenuKeys.SKILLS,
                    FabricMenuKeys.STATS,
                    FabricMenuKeys.LEVEL_PROGRESSION,
                    FabricMenuKeys.STAT_INFO,
                    FabricMenuKeys.SOURCES,
                    FabricMenuKeys.UNCLAIMED_ITEMS);

    private FabricCommandBuiltInMenuId() {}

    public static Optional<String> resolveForOpenCommand(String raw) {
        if (raw == null) {
            return Optional.empty();
        }
        String t = raw.trim().toLowerCase(Locale.ROOT);
        if (OPENVIA_COMMAND.contains(t)) {
            return Optional.of(t);
        }
        return Optional.empty();
    }

    /** True when opening by id requires skill/stat args (typed Brigadier nodes; see Sprint 45). */
    public static boolean requiresContext(String normalizedMenuId) {
        return FabricMenuKeys.LEVEL_PROGRESSION.equals(normalizedMenuId)
                || FabricMenuKeys.STAT_INFO.equals(normalizedMenuId)
                || FabricMenuKeys.SOURCES.equals(normalizedMenuId);
    }
}
