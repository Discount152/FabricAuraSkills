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

    /**
     * Granite {@code /backup} family: allow non-player sources, or any player with either
     * {@link FabricCommandPermissions#BACKUP} (create/save/list/info) or
     * {@link FabricCommandPermissions#BACKUP_LOAD} (load/restore), so the root can show usage and
     * children remain correctly gated (**Sprint 85**).
     */
    public static Predicate<ServerCommandSource> backupFamilyAccess() {
        return src -> {
            if (!src.isExecutedByPlayer()) {
                return true;
            }
            return Permissions.check(src, FabricCommandPermissions.BACKUP, false)
                    || Permissions.check(src, FabricCommandPermissions.BACKUP_LOAD, false);
        };
    }

    /**
     * Any {@code auraskills.command.item.*} node used by the Fabric {@code item} subtree
     * (modifier/trait, ignore, requirement, multiplier, register, give). Non-player sources always
     * pass so console can see usage (**Sprint 85**).
     */
    public static Predicate<ServerCommandSource> anyItemSubcommandPermission() {
        return src -> {
            if (!src.isExecutedByPlayer()) {
                return true;
            }
            return Permissions.check(src, FabricCommandPermissions.ITEM_MODIFIER, false)
                    || Permissions.check(src, FabricCommandPermissions.ITEM_IGNORE, false)
                    || Permissions.check(src, FabricCommandPermissions.ITEM_REQUIREMENT, false)
                    || Permissions.check(src, FabricCommandPermissions.ITEM_MULTIPLIER, false)
                    || Permissions.check(src, FabricCommandPermissions.ITEM_REGISTER, false)
                    || Permissions.check(src, FabricCommandPermissions.ITEM_GIVE, false);
        };
    }

    public static boolean isPlayer(ServerCommandSource source) {
        return source.getEntity() instanceof ServerPlayerEntity;
    }
}
