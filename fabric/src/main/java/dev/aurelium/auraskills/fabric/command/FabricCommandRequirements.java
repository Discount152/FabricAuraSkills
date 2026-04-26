package dev.aurelium.auraskills.fabric.command;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.function.Predicate;

/**
 * Shared Brigadier {@code requires} predicates and small {@link ServerCommandSource}
 * checks for cross-platform (Java + Geyser/Bedrock) server-side command handling.
 * Feedback stays plain {@link net.minecraft.text.Text} (no client-only
 * assumptions).
 */
public final class FabricCommandRequirements {

    private FabricCommandRequirements() {}

    public static Predicate<ServerCommandSource> permission(String node, boolean defaultGrant) {
        return src -> Permissions.check(src, node, defaultGrant);
    }

    /**
     * Like {@link #permission} but allows non-player sources (console, RCON, functions) without an
     * explicit provider grant. Many permission backends do not attach {@code auraskills.command.*}
     * nodes to the console {@link ServerCommandSource}; admin commands would otherwise always fail
     * from the server console even for operators.
     */
    public static Predicate<ServerCommandSource> permissionOrNonPlayerSource(
            String node, boolean defaultGrant) {
        return src -> {
            if (!src.isExecutedByPlayer()) {
                return true;
            }
            return Permissions.check(src, node, defaultGrant);
        };
    }

    public static boolean isPlayer(ServerCommandSource source) {
        return source.getEntity() instanceof ServerPlayerEntity;
    }
}
