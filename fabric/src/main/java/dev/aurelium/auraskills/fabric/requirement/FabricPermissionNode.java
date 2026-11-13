package dev.aurelium.auraskills.fabric.requirement;

import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.FabricUser;
import net.minecraft.server.level.ServerPlayer;

final class FabricPermissionNode extends FabricRequirementNode {

    private final String permission;

    FabricPermissionNode(FabricAuraSkills plugin, String permission, String message) {
        super(plugin, message);
        this.permission = permission;
    }

    @Override
    public boolean check(ServerPlayer player) {
        if (permission == null) {
            return true;
        }
        if (plugin.getUserManager().getUser(player.getUUID()) instanceof FabricUser user) {
            return user.hasPermission(permission);
        }
        return false;
    }
}
