package dev.granite.graniteskills.fabric;

import dev.granite.graniteskills.common.util.text.TextUtil;

/**
 * Grindstone XP. Only a result-slot take with disenchantable levels awards XP. Any other click,
 * and a grind whose total level is zero, awards nothing.
 */
public final class GrindstoneXpAward {

    private GrindstoneXpAward() {
    }

    public static boolean isResultTake(boolean resultSlot, boolean outputWasPresent, boolean outputChanged) {
        return resultSlot && outputWasPresent && outputChanged;
    }

    public static double xp(double sourceXp, String multiplier, int totalLevel) {
        if (sourceXp <= 0.0) {
            return 0.0;
        }
        if (multiplier == null) {
            return sourceXp;
        }
        String replaced = TextUtil.replace(multiplier, "{total_level}", String.valueOf(Math.max(0, totalLevel)));
        try {
            double factor = Double.parseDouble(replaced);
            if (factor <= 0.0) {
                return 0.0;
            }
            return sourceXp * factor;
        } catch (NumberFormatException ignored) {
            return 0.0;
        }
    }

    public static double award(boolean resultTake, double sourceXp, String multiplier, int totalLevel) {
        if (!resultTake) {
            return 0.0;
        }
        return xp(sourceXp, multiplier, totalLevel);
    }
}
