package dev.granite.graniteskills.fabric.trait;

import dev.granite.graniteskills.api.trait.Trait;
import dev.granite.graniteskills.api.trait.Traits;
import dev.granite.graniteskills.api.util.NumberUtil;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import net.minecraft.server.level.ServerPlayer;

import java.util.Locale;
import java.util.Random;

public final class FabricCritChanceTrait extends FabricTraitImpl {

    private final Random random = new Random();

    public FabricCritChanceTrait(FabricGraniteSkills plugin) {
        super(plugin, Traits.CRIT_CHANCE);
    }

    @Override
    public double getBaseLevel(ServerPlayer player, Trait trait) {
        return Traits.CRIT_CHANCE.optionDouble("base");
    }

    @Override
    public String getMenuDisplay(double value, Trait trait, Locale locale) {
        return NumberUtil.format1(value) + "%";
    }

    public boolean isCrit(User user) {
        return random.nextDouble() < (user.getEffectiveTraitLevel(Traits.CRIT_CHANCE) / 100.0);
    }
}
