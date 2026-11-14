package dev.granite.graniteskills.fabric.command;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Predicate;

/**
 * Shared Brigadier {@code requires} predicates and small {@link CommandSourceStack}
 * checks for cross-platform (Java + Geyser/Bedrock) server-side command handling.
 * Feedback stays plain {@link net.minecraft.network.chat.Component} (no client-only
 * assumptions).
 */
public final class FabricCommandRequirements {

    private FabricCommandRequirements() {}

    public static Predicate<CommandSourceStack> permission(String node, boolean defaultGrant) {
        return src -> Permissions.check(src, node, defaultGrant);
    }

    /**
     * Like {@link #permission} but falls back to the vanilla operator level when no permission
     * provider is installed. {@code permission(node, false)} denies operators outright on servers
     * without LuckPerms, which also hides the node from tab-completion.
     */
    public static Predicate<CommandSourceStack> permissionOrOpLevel(String node, int opLevel) {
        return src -> Permissions.check(src, node, opLevel);
    }

    /**
     * Like {@link #permission} but allows non-player sources (console, RCON, functions) without an
     * explicit provider grant. Many permission backends do not attach {@code graniteskills.command.*}
     * nodes to the console {@link CommandSourceStack}; admin commands would otherwise always fail
     * from the server console even for operators.
     */
    public static Predicate<CommandSourceStack> permissionOrNonPlayerSource(
            String node, boolean defaultGrant) {
        return src -> {
            if (!src.isPlayer()) {
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
    public static Predicate<CommandSourceStack> backupFamilyAccess() {
        return src -> {
            if (!src.isPlayer()) {
                return true;
            }
            return Permissions.check(src, FabricCommandPermissions.BACKUP, false)
                    || Permissions.check(src, FabricCommandPermissions.BACKUP_LOAD, false);
        };
    }

    /**
     * Any {@code graniteskills.command.item.*} node used by the Fabric {@code item} subtree
     * (modifier/trait, ignore, requirement, multiplier, register, give). Non-player sources always
     * pass so console can see usage (**Sprint 85**).
     */
    public static Predicate<CommandSourceStack> anyItemSubcommandPermission() {
        return src -> {
            if (!src.isPlayer()) {
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

    public static boolean isPlayer(CommandSourceStack source) {
        return source.getEntity() instanceof ServerPlayer;
    }
}
