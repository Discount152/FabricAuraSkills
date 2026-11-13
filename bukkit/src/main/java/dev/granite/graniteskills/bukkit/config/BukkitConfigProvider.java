package dev.granite.graniteskills.bukkit.config;

import dev.granite.graniteskills.bukkit.GraniteSkills;
import dev.granite.graniteskills.bukkit.hooks.BukkitHookRegistrar;
import dev.granite.graniteskills.bukkit.hooks.Hooks;
import dev.granite.graniteskills.common.config.ConfigProvider;
import dev.granite.graniteskills.common.config.Option;
import org.bukkit.ChatColor;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.Locale;
import java.util.Map;

public class BukkitConfigProvider extends ConfigProvider {

    private final GraniteSkills plugin;

    public BukkitConfigProvider(GraniteSkills plugin, Map<Option, Object> overrides) {
        super(plugin, overrides);
        this.plugin = plugin;
    }

    @Override
    public void registerHooks(ConfigurationNode config) {
        BukkitHookRegistrar hookRegistrar = new BukkitHookRegistrar(plugin, plugin.getHookManager());
        hookRegistrar.registerHooks(config.node("hooks"), Hooks.values());
    }

    @Override
    public Object parseColor(String value) {
        return ChatColor.valueOf(value.toUpperCase(Locale.ROOT));
    }

}
