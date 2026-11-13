package dev.aurelium.auraskills.fabric.trait;

import dev.aurelium.auraskills.api.trait.Trait;
import dev.aurelium.auraskills.api.trait.Traits;
import dev.aurelium.auraskills.api.util.NumberUtil;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import net.minecraft.server.level.ServerPlayer;

import java.util.Locale;

/**
 * Menu/base display only; damage math runs in {@link dev.aurelium.auraskills.fabric.damage.FabricAttackDamageApplier}.
 */
public final class FabricAttackDamageTrait extends FabricTraitImpl {

    public FabricAttackDamageTrait(FabricAuraSkills plugin) {
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
