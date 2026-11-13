package dev.granite.graniteskills.bukkit.hooks;

import dev.granite.graniteskills.bukkit.GraniteSkills;
import dev.granite.graniteskills.common.hooks.Hook;
import dev.granite.graniteskills.common.hooks.HookManager;
import dev.granite.graniteskills.common.hooks.HookRegistrar;
import org.bukkit.event.Listener;

public class BukkitHookRegistrar extends HookRegistrar {

    private final GraniteSkills plugin;

    public BukkitHookRegistrar(GraniteSkills plugin, HookManager manager) {
        super(plugin, manager);
        this.plugin = plugin;
    }

    @Override
    public void registerEvents(Hook hook) {
        if (hook instanceof Listener) {
            plugin.getServer().getPluginManager().registerEvents((Listener) hook, plugin);
        }
    }

    @Override
    public boolean isPluginEnabled(String name) {
        return plugin.getServer().getPluginManager().isPluginEnabled(name);
    }

    @Override
    public boolean isPluginLoaded(String name) {
        return plugin.getServer().getPluginManager().getPlugin(name) != null;
    }

}
