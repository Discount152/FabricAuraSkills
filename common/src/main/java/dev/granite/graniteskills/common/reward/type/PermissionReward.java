package dev.granite.graniteskills.common.reward.type;

import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import dev.granite.graniteskills.common.hooks.PermissionsHook;
import dev.granite.graniteskills.common.user.User;

public class PermissionReward extends MessagedReward {

    private final String permission;
    private final boolean value;

    public PermissionReward(GraniteSkillsPlugin plugin, Skill skill, String menuMessage, String chatMessage, String permission, boolean value) {
        super(plugin, skill, menuMessage, chatMessage);
        this.permission = permission;
        this.value = value;
    }

    @Override
    public void giveReward(User user, Skill skill, int level) {
        if (hooks.isRegistered(PermissionsHook.class)) {
            hooks.getHook(PermissionsHook.class).setPermission(user, permission, value);
        }
    }

    public String getPermission() {
        return permission;
    }

    public boolean getValue() {
        return value;
    }

}
