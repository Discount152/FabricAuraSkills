package dev.granite.graniteskills.bukkit.requirement;

import dev.granite.graniteskills.bukkit.GraniteSkills;
import org.bukkit.entity.Player;

public class PermissionNode extends RequirementNode {

    private final String permission;

    public PermissionNode(GraniteSkills plugin, String permission, String message) {
        super(plugin, message);
        this.permission = permission;
    }

    @Override
    public boolean check(Player player) {
        if (permission == null) {
            return true;
        }

        return player.hasPermission(permission);
    }

}
