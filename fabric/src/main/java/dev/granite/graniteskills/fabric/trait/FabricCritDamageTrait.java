package dev.granite.graniteskills.fabric.trait;

import dev.granite.graniteskills.api.trait.Trait;
import dev.granite.graniteskills.api.trait.Traits;
import dev.granite.graniteskills.api.util.NumberUtil;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import net.minecraft.server.level.ServerPlayer;

import java.util.Locale;

public final class FabricCritDamageTrait extends FabricTraitImpl {

    public FabricCritDamageTrait(FabricGraniteSkills plugin) {
        super(plugin, Traits.CRIT_DAMAGE);
    }

    @Override
    public double getBaseLevel(ServerPlayer player, Trait trait) {
        return Traits.CRIT_DAMAGE.optionDouble("base");
    }

    @Override
    public String getMenuDisplay(double value, Trait trait, Locale locale) {
        return NumberUtil.format1(value) + "%";
    }
}
