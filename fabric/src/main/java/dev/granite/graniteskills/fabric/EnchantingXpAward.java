package dev.granite.graniteskills.fabric;

/**
 * Enchanting XP from the source amount and its configured unit. No added enchant levels, and no
 * experience levels spent when that is the unit, award nothing.
 */
public final class EnchantingXpAward {

    public static final String ENCHANT_LEVEL = "{sources.units.enchant_level}";
    public static final String EXP_REQUIREMENT = "{sources.units.exp_requirement}";
    /** Bukkit omits the closing brace. Keep that spelling so old configs still match. */
    public static final String EXP_REQUIREMENT_TYPO = "{sources.units.exp_requirement";

    private EnchantingXpAward() {
    }

    public static double xp(double sourceXp, String unit, int enchantLevelsAdded, int levelsSpent) {
        if (sourceXp <= 0.0) {
            return 0.0;
        }
        if (ENCHANT_LEVEL.equals(unit)) {
            if (enchantLevelsAdded <= 0) {
                return 0.0;
            }
            return sourceXp * enchantLevelsAdded;
        }
        if (EXP_REQUIREMENT.equals(unit) || EXP_REQUIREMENT_TYPO.equals(unit)) {
            if (levelsSpent <= 0) {
                return 0.0;
            }
            return sourceXp * levelsSpent;
        }
        return sourceXp;
    }
}
