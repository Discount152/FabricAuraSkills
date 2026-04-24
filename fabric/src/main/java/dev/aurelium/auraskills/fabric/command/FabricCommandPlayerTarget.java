package dev.aurelium.auraskills.fabric.command;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

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

    public static Optional<ServerPlayerEntity> findOnline(MinecraftServer server, String name) {
        if (name == null || name.isEmpty()) {
            return Optional.empty();
        }
        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
            if (p.getName().getString().equalsIgnoreCase(name)) {
                return Optional.of(p);
            }
        }
        return Optional.empty();
    }

    public static List<String> onlinePlayerNames(MinecraftServer server) {
        List<String> out = new ArrayList<>();
        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
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
