package dev.granite.graniteskills.common.reward.builder;

import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import dev.granite.graniteskills.common.reward.SkillReward;
import dev.granite.graniteskills.common.reward.type.PermissionReward;
import dev.granite.graniteskills.common.util.data.Validate;

public class PermissionRewardBuilder extends MessagedRewardBuilder {

    private String permission;
    private boolean value;

    public PermissionRewardBuilder(GraniteSkillsPlugin plugin) {
        super(plugin);
        this.value = true;
    }

    public PermissionRewardBuilder permission(String permission) {
        this.permission = permission;
        return this;
    }

    public PermissionRewardBuilder value(boolean value) {
        this.value = value;
        return this;
    }

    @Override
    public SkillReward build() {
        Validate.notNull(permission, "You must specify a permission");
        return new PermissionReward(plugin, skill, menuMessage, chatMessage, permission, value);
    }

}
