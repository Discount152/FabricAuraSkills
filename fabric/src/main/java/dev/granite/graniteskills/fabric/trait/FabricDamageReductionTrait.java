package dev.granite.graniteskills.fabric.trait;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.parser.ParseException;
import dev.granite.graniteskills.api.trait.Trait;
import dev.granite.graniteskills.api.trait.Traits;
import dev.granite.graniteskills.api.util.NumberUtil;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import dev.granite.graniteskills.fabric.damage.FabricDamageReductionApplier;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * Menu/base display and formula reset; reduction math runs in {@link FabricDamageReductionApplier}.
 */
public final class FabricDamageReductionTrait extends FabricTraitImpl {

    private @Nullable Expression menuFormula;

    public FabricDamageReductionTrait(FabricGraniteSkills plugin) {
        super(plugin, Traits.DAMAGE_REDUCTION);
    }

    @Override
    public double getBaseLevel(ServerPlayer player, Trait trait) {
        return 0;
    }

    @Override
    public String getMenuDisplay(double value, Trait trait, Locale locale) {
        return NumberUtil.format1(getReductionValue(value) * 100.0) + "%";
    }

    @Override
    public boolean displayMatchesValue() {
        return false;
    }

    @Override
    protected void reload(ServerPlayer player, Trait trait) {
        FabricDamageReductionApplier.resetFormula();
        menuFormula = null;
    }

    private double getReductionValue(double value) {
        Trait trait = Traits.DAMAGE_REDUCTION;
        try {
            if (menuFormula == null) {
                menuFormula = new Expression(trait.optionString("formula"));
            }
            menuFormula.with("value", value);
            return menuFormula.evaluate().getNumberValue().doubleValue();
        } catch (EvaluationException | ParseException | UnsupportedOperationException e) {
            plugin.logger().warn("Failed to evaluate formula for trait graniteskills/damage_reduction: " + e.getMessage());
        }
        return -1.0 * Math.pow(1.01, -1.0 * value) + 1.0;
    }
}
