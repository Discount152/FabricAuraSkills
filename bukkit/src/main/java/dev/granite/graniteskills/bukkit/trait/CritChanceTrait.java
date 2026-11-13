package dev.granite.graniteskills.bukkit.trait;

import dev.granite.graniteskills.api.trait.Trait;
import dev.granite.graniteskills.api.trait.Traits;
import dev.granite.graniteskills.api.util.NumberUtil;
import dev.granite.graniteskills.bukkit.GraniteSkills;
import dev.granite.graniteskills.common.user.User;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.Random;

public class CritChanceTrait extends TraitImpl {

    private final Random rand = new Random();

    CritChanceTrait(GraniteSkills plugin) {
        super(plugin, Traits.CRIT_CHANCE);
    }

    @Override
    public double getBaseLevel(Player player, Trait trait) {
        return Traits.CRIT_CHANCE.optionDouble("base");
    }

    @Override
    public String getMenuDisplay(double value, Trait trait, Locale locale) {
        return NumberUtil.format1(value) + "%";
    }

    public boolean isCrit(User user) {
        return rand.nextDouble() < (user.getEffectiveTraitLevel(Traits.CRIT_CHANCE) / 100);
    }

}
