package dev.granite.graniteskills.common.hooks;

import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import dev.granite.graniteskills.common.user.User;
import org.spongepowered.configurate.ConfigurationNode;

public abstract class PermissionsHook extends Hook {

    public PermissionsHook(GraniteSkillsPlugin plugin, ConfigurationNode config) {
        super(plugin, config);
    }

    public abstract void setPermission(User user, String permission, boolean value);

    public abstract void unsetPermission(User user, String permission, boolean value);

}
