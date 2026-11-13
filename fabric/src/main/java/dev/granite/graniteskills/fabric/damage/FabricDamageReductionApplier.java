package dev.granite.graniteskills.fabric.damage;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.parser.ParseException;
import dev.granite.graniteskills.api.damage.DamageModifier;
import dev.granite.graniteskills.api.trait.Trait;
import dev.granite.graniteskills.api.trait.Traits;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public final class FabricDamageReductionApplier {

    private static @Nullable Expression formula;

    private FabricDamageReductionApplier() {
    }

    public static void resetFormula() {
        formula = null;
    }

    public static void apply(FabricGraniteSkills plugin, FabricDamageMeta meta, ServerPlayer victim) {
        if (!Traits.DAMAGE_REDUCTION.isEnabled()) {
            return;
        }
        User user = plugin.getUserManager().getUser(victim.getUUID());
        double reduction = user.getEffectiveTraitLevel(Traits.DAMAGE_REDUCTION);
        double factor = (1.0 - getReductionValue(plugin, reduction)) - 1.0;
        meta.addDefenseModifier(new DamageModifier(factor, DamageModifier.Operation.MULTIPLY));
    }

    private static synchronized double getReductionValue(FabricGraniteSkills plugin, double value) {
        Trait trait = Traits.DAMAGE_REDUCTION;
        try {
            if (formula == null) {
                formula = new Expression(trait.optionString("formula"));
            }
            formula.with("value", value);
            return formula.evaluate().getNumberValue().doubleValue();
        } catch (EvaluationException | ParseException | UnsupportedOperationException e) {
            plugin.logger().warn("Failed to evaluate formula for trait graniteskills/damage_reduction: " + e.getMessage());
        }
        return -1.0 * Math.pow(1.01, -1.0 * value) + 1.0;
    }
}
