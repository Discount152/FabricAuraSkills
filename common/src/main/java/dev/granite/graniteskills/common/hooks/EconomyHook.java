package dev.granite.graniteskills.common.hooks;

import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import dev.granite.graniteskills.common.user.User;
import org.spongepowered.configurate.ConfigurationNode;

public abstract class EconomyHook extends Hook {

    public EconomyHook(GraniteSkillsPlugin plugin, ConfigurationNode config) {
        super(plugin, config);
    }

    public abstract void deposit(User user, double amount);

}
