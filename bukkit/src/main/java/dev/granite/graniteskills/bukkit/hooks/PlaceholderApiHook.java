package dev.granite.graniteskills.bukkit.hooks;

import dev.granite.graniteskills.bukkit.GraniteSkills;
import dev.granite.graniteskills.bukkit.user.BukkitUser;
import dev.granite.graniteskills.common.hooks.Hook;
import dev.granite.graniteskills.common.hooks.PlaceholderHook;
import dev.granite.graniteskills.common.user.User;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
import org.spongepowered.configurate.ConfigurationNode;

public class PlaceholderApiHook extends PlaceholderHook {

    public PlaceholderApiHook(GraniteSkills plugin, ConfigurationNode config) {
        super(plugin, config);
        if (!plugin.getServer().getPluginManager().isPluginEnabled(Hooks.PLACEHOLDER_API.getPluginName())) {
            // Defer if plugin is loaded but not enabled yet
            plugin.getScheduler().executeSync(() -> {
                new PlaceholderApiProvider(plugin, "graniteskills").register();
                new PlaceholderApiProvider(plugin, "aureliumskills").register();
            });
        } else {
            new PlaceholderApiProvider(plugin, "graniteskills").register();
            new PlaceholderApiProvider(plugin, "aureliumskills").register();
        }
    }

    @Override
    public String setPlaceholders(User user, String message) {
        Player player = ((BukkitUser) user).getPlayer();
        if (player != null) {
            return PlaceholderAPI.setPlaceholders(player, message);
        } else {
            return message;
        }
    }

    @Override
    public Class<? extends Hook> getTypeClass() {
        return PlaceholderHook.class;
    }

}
