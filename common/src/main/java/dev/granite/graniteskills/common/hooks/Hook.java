package dev.granite.graniteskills.common.hooks;

import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import org.spongepowered.configurate.ConfigurationNode;

public abstract class Hook {

    protected final GraniteSkillsPlugin plugin;
    private final ConfigurationNode config;

    public Hook(GraniteSkillsPlugin plugin, ConfigurationNode config) {
        this.plugin = plugin;
        this.config = config;
    }

    public ConfigurationNode getConfig() {
        return config;
    }

    public abstract Class<? extends Hook> getTypeClass();

}
