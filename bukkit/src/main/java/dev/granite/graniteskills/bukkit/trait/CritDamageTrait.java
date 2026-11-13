package dev.granite.graniteskills.bukkit.trait;

import dev.granite.graniteskills.api.trait.Trait;
import dev.granite.graniteskills.api.trait.Traits;
import dev.granite.graniteskills.api.util.NumberUtil;
import dev.granite.graniteskills.bukkit.GraniteSkills;
import org.bukkit.entity.Player;

import java.util.Locale;

public class CritDamageTrait extends TraitImpl {

    CritDamageTrait(GraniteSkills plugin) {
        super(plugin, Traits.CRIT_DAMAGE);
    }

    @Override
    public double getBaseLevel(Player player, Trait trait) {
        return Traits.CRIT_DAMAGE.optionDouble("base");
    }

    @Override
    public String getMenuDisplay(double value, Trait trait, Locale locale) {
        return NumberUtil.format1(value) + "%";
    }

}
