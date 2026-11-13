package dev.granite.graniteskills.bukkit.hooks;

import dev.granite.graniteskills.bukkit.GraniteSkills;
import dev.granite.graniteskills.bukkit.user.BukkitUser;
import dev.granite.graniteskills.common.hooks.EconomyHook;
import dev.granite.graniteskills.common.hooks.Hook;
import dev.granite.graniteskills.common.user.User;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;

public class VaultHook extends EconomyHook {

    @Nullable
    private final Economy economy;

    public VaultHook(GraniteSkills plugin, ConfigurationNode config) {
        super(plugin, config);
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp != null) {
            economy = rsp.getProvider();
        } else {
            economy = null;
        }
    }

    @Override
    public void deposit(User user, double amount) {
        Player player = ((BukkitUser) user).getPlayer();
        if (player != null && economy != null) {
            economy.depositPlayer(player, amount);
        }
    }

    @Override
    public Class<? extends Hook> getTypeClass() {
        return EconomyHook.class;
    }

}
