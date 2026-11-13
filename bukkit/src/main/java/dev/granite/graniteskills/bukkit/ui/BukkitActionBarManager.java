package dev.granite.graniteskills.bukkit.ui;

import dev.granite.graniteskills.api.trait.Traits;
import dev.granite.graniteskills.bukkit.user.BukkitUser;
import dev.granite.graniteskills.bukkit.util.AttributeCompat;
import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import dev.granite.graniteskills.common.ui.ActionBarManager;
import dev.granite.graniteskills.common.ui.UiProvider;
import dev.granite.graniteskills.common.user.User;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BukkitActionBarManager extends ActionBarManager {

    public BukkitActionBarManager(GraniteSkillsPlugin plugin, UiProvider uiProvider) {
        super(plugin, uiProvider);
    }

    @Override
    @NotNull
    public String getHp(User user) {
        Player player = ((BukkitUser) user).getPlayer();
        if (player == null) return "";

        return String.valueOf(Math.round(player.getHealth() * Traits.HP.optionDouble("action_bar_scaling", 1)));
    }

    @Override
    @NotNull
    public String getMaxHp(User user) {
        Player player = ((BukkitUser) user).getPlayer();
        if (player == null) return "";

        AttributeInstance attribute = player.getAttribute(AttributeCompat.maxHealth);
        if (attribute != null) {
            return String.valueOf(Math.round(attribute.getValue() * Traits.HP.optionDouble("action_bar_scaling", 1)));
        }
        return "";
    }

    @Override
    @NotNull
    public String getWorldName(User user) {
        Player player = ((BukkitUser) user).getPlayer();
        if (player != null) {
            return player.getWorld().getName();
        }
        return "";
    }

}
