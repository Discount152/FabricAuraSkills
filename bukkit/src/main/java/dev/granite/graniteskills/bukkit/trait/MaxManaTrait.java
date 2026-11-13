package dev.granite.graniteskills.bukkit.trait;

import dev.granite.graniteskills.api.trait.Trait;
import dev.granite.graniteskills.api.trait.Traits;
import dev.granite.graniteskills.bukkit.GraniteSkills;
import dev.granite.graniteskills.common.user.User;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class MaxManaTrait extends TraitImpl {

    MaxManaTrait(GraniteSkills plugin) {
        super(plugin, Traits.MAX_MANA);
    }

    @Override
    public double getBaseLevel(Player player, Trait trait) {
        return trait.optionDouble("base", 20);
    }

    @Override
    protected void reload(Player player, Trait trait) {
        if (!trait.optionBoolean("allow_overflow", false)) {
            User user = plugin.getUser(player);
            // Remove overflow mana
            double maxMana = user.getMaxMana();
            if (user.getMana() > maxMana) {
                user.setMana(maxMana);
            }
        }
    }

    @Override
    protected void changeWorld(PlayerChangedWorldEvent event, Trait trait) {
        if (!trait.equals(Traits.MAX_MANA)) return;

        reload(event.getPlayer(), trait);
    }

}
