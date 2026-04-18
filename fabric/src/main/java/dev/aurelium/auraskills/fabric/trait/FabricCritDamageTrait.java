package dev.aurelium.auraskills.fabric.trait;

import dev.aurelium.auraskills.api.trait.Trait;
import dev.aurelium.auraskills.api.trait.Traits;
import dev.aurelium.auraskills.api.util.NumberUtil;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Locale;

public final class FabricCritDamageTrait extends FabricTraitImpl {

    public FabricCritDamageTrait(FabricAuraSkills plugin) {
        super(plugin, Traits.CRIT_DAMAGE);
    }

    @Override
    public double getBaseLevel(ServerPlayerEntity player, Trait trait) {
        return Traits.CRIT_DAMAGE.optionDouble("base");
    }

    @Override
    public String getMenuDisplay(double value, Trait trait, Locale locale) {
        return NumberUtil.format1(value) + "%";
    }
}
