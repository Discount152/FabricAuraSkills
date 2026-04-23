package dev.aurelium.auraskills.fabric.trait;

import dev.aurelium.auraskills.api.trait.Trait;
import dev.aurelium.auraskills.api.trait.Traits;
import dev.aurelium.auraskills.api.util.NumberUtil;
import dev.aurelium.auraskills.fabric.FabricAnvilDiscount;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Locale;

/**
 * Bukkit {@code AnvilDiscountTrait} counterpart — cost adjustment runs in
 * {@code AnvilScreenHandlerAnvilDiscountMixin} via {@link FabricAnvilDiscount#afterUpdateResult}.
 */
public final class FabricAnvilDiscountTrait extends FabricTraitImpl {

    public FabricAnvilDiscountTrait(FabricAuraSkills plugin) {
        super(plugin, Traits.ANVIL_DISCOUNT);
    }

    @Override
    public double getBaseLevel(ServerPlayerEntity player, Trait trait) {
        return 0.0;
    }

    @Override
    public String getMenuDisplay(double value, Trait trait, Locale locale) {
        return NumberUtil.format1(FabricAnvilDiscount.getDiscountForTraitValue(value, plugin.logger()) * 100.0) + "%";
    }

    @Override
    public boolean displayMatchesValue() {
        return false;
    }

    @Override
    protected void reload(ServerPlayerEntity player, Trait trait) {
        FabricAnvilDiscount.resetFormula();
    }
}
