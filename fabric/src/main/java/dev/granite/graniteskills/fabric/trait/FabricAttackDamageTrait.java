package dev.granite.graniteskills.fabric.trait;

import dev.granite.graniteskills.api.trait.Trait;
import dev.granite.graniteskills.api.trait.Traits;
import dev.granite.graniteskills.api.util.NumberUtil;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import net.minecraft.server.level.ServerPlayer;

import java.util.Locale;

/**
 * Menu/base display only; damage math runs in {@link dev.granite.graniteskills.fabric.damage.FabricAttackDamageApplier}.
 */
public final class FabricAttackDamageTrait extends FabricTraitImpl {

    public FabricAttackDamageTrait(FabricGraniteSkills plugin) {
        super(plugin, Traits.ATTACK_DAMAGE);
    }

    @Override
    public double getBaseLevel(ServerPlayer player, Trait trait) {
        return 0;
    }

    @Override
    public String getMenuDisplay(double value, Trait trait, Locale locale) {
        if (Traits.ATTACK_DAMAGE.optionBoolean("use_percent")) {
            return "+" + NumberUtil.format1(value) + "%";
        }
        return "+" + NumberUtil.format1(value);
    }
}
