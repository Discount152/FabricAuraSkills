package dev.aurelium.auraskills.fabric.command;

import dev.aurelium.auraskills.fabric.menu.FabricMenuKeys;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * Whitelist + normalization for **bounded** built-in {@code /openmenu}-style access (**Sprint
 * 44**). Only menu ids the Fabric registry can open without extra {@link
 * dev.aurelium.auraskills.fabric.menu.FabricMenuContext} (see {@link FabricMenuKeys}).
 */
public final class FabricCommandBuiltInMenuId {

    /** Menu ids currently allowed for {@code /openmenu} / {@code <alias> openmenu} (Sprint 44). */
    public static final Set<String> OPENVIA_COMMAND = Set.of(FabricMenuKeys.SKILLS, FabricMenuKeys.STATS);

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
}
