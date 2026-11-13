package dev.granite.graniteskills.common.hooks;

import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import dev.granite.graniteskills.common.user.User;
import org.spongepowered.configurate.ConfigurationNode;

public abstract class PlaceholderHook extends Hook {

    public PlaceholderHook(GraniteSkillsPlugin plugin, ConfigurationNode config) {
        super(plugin, config);
    }

    public abstract String setPlaceholders(User user, String message);

}
