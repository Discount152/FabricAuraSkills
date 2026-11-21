package dev.granite.graniteskills.fabric.command;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Resolves and suggests **online** players for Brigadier. Does not call offline
 * lookups (matches common Bukkit command patterns where targets are in-world).
 */
public final class FabricCommandPlayerTarget {

    private FabricCommandPlayerTarget() {}

    public static Optional<ServerPlayer> findOnline(MinecraftServer server, String name) {
        if (name == null || name.isEmpty()) {
            return Optional.empty();
        }
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            if (p.getName().getString().equalsIgnoreCase(name)) {
                return Optional.of(p);
            }
        }
        return Optional.empty();
    }

    public static List<String> onlinePlayerNames(MinecraftServer server) {
        List<String> out = new ArrayList<>();
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            out.add(p.getName().getString());
        }
        return out;
    }

    /** Suggest names; optional prefix from partially typed first characters (case-insensitive). */
    public static List<String> filterPrefix(List<String> names, String remaining) {
        if (remaining == null || remaining.isEmpty()) {
            return names;
        }
        String prefix = remaining.toLowerCase(Locale.ROOT);
        return names.stream()
                .filter(n -> n.toLowerCase(Locale.ROOT).startsWith(prefix))
                .toList();
    }
}
