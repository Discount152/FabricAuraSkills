package dev.granite.graniteskills.bukkit.trait;

import dev.granite.graniteskills.api.trait.Trait;
import dev.granite.graniteskills.api.trait.Traits;
import dev.granite.graniteskills.api.util.NumberUtil;
import dev.granite.graniteskills.bukkit.GraniteSkills;
import dev.granite.graniteskills.common.user.User;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerExpChangeEvent;

import java.util.Locale;

public class ExperienceBonusTrait extends TraitImpl {

    ExperienceBonusTrait(GraniteSkills plugin) {
        super(plugin, Traits.EXPERIENCE_BONUS);
    }

    @Override
    public double getBaseLevel(Player player, Trait trait) {
        return 0;
    }

    @Override
    public String getMenuDisplay(double value, Trait trait, Locale locale) {
        return "+" + NumberUtil.format1(value) + "%";
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerExpChange(PlayerExpChangeEvent event) {
        Player player = event.getPlayer();
        //Check for disabled world
        if (plugin.getWorldManager().isInDisabledWorld(player.getLocation())) {
            return;
        }
        User user = plugin.getUser(player);
        double bonus = user.getEffectiveTraitLevel(Traits.EXPERIENCE_BONUS) / 100;
        event.setAmount((int) (event.getAmount() * (1 + bonus)));
    }

}
