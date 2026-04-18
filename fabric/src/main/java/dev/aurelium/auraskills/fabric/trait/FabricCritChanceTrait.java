package dev.aurelium.auraskills.fabric.trait;

import dev.aurelium.auraskills.api.trait.Trait;
import dev.aurelium.auraskills.api.trait.Traits;
import dev.aurelium.auraskills.api.util.NumberUtil;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Locale;
import java.util.Random;

public final class FabricCritChanceTrait extends FabricTraitImpl {

    private final Random random = new Random();

    public FabricCritChanceTrait(FabricAuraSkills plugin) {
        super(plugin, Traits.CRIT_CHANCE);
    }

    @Override
    public double getBaseLevel(ServerPlayerEntity player, Trait trait) {
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
