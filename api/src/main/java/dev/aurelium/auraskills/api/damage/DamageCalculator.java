package dev.aurelium.auraskills.api.damage;

import java.util.List;

/**
 * Shared damage math used by Bukkit {@code DamageEvent} and the Fabric damage pipeline.
 */
public final class DamageCalculator {

    private DamageCalculator() {
    }

    public static double computeModifiedAttackDamage(double baseDamage, List<DamageModifier> attackModifiers) {
        DamageCalculation calc = new DamageCalculation(baseDamage);
        double additive = 0.0;
        for (DamageModifier modifier : attackModifiers) {
            additive += applyModifier(calc, modifier);
        }
        return calc.getDamage() * (1 + additive);
    }

    public static double computeModifiedDamage(double baseDamage, List<DamageModifier> attackModifiers,
            List<DamageModifier> defenseModifiers) {
        DamageCalculation calc = new DamageCalculation(baseDamage);
        double additive = 0.0;
        for (DamageModifier modifier : attackModifiers) {
            additive += applyModifier(calc, modifier);
        }
        for (DamageModifier modifier : defenseModifiers) {
            additive += applyModifier(calc, modifier);
        }
        return calc.getDamage() * (1 + additive);
    }

    private static double applyModifier(DamageCalculation calculation, DamageModifier modifier) {
        switch (modifier.operation()) {
            case MULTIPLY: {
                double multiplier = 1.0 + modifier.value();
                calculation.setDamage(calculation.getDamage() * multiplier);
                break;
            }
            case ADD_BASE:
                calculation.setDamage(calculation.getDamage() + modifier.value());
                break;
            case ADD_COMBINED:
                return modifier.value();
            case NONE:
            default:
                break;
        }
        return 0.0;
    }

    private static final class DamageCalculation {

        private double damage;

        DamageCalculation(double baseDamage) {
            this.damage = baseDamage;
        }

        private void setDamage(double damage) {
            this.damage = damage;
        }

        public double getDamage() {
            return damage;
        }
    }
}
