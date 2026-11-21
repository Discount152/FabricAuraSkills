package dev.granite.graniteskills.fabric.ability;

/**
 * Growth Aura's extra crop stages. A player outside the configured radius does not apply them.
 */
public final class GrowthAuraAward {

    private GrowthAuraAward() {
    }

    public static boolean inRange(double distanceSquared, int radius) {
        if (radius < 0 || distanceSquared < 0.0) {
            return false;
        }
        double limit = (double) radius * radius;
        return distanceSquared <= limit;
    }

    public static int stages(double abilityValue, double roll) {
        if (abilityValue <= 0.0) {
            return 0;
        }
        int guaranteed = (int) (abilityValue / 100.0);
        double chance = (abilityValue - guaranteed * 100.0) / 100.0;
        int extra = roll < chance ? 1 : 0;
        return guaranteed + extra;
    }

    public static int nextAge(int age, int maxAge, int extraStages, boolean inRange) {
        if (!inRange || extraStages <= 0 || age >= maxAge) {
            return age;
        }
        return Math.min(age + extraStages, maxAge);
    }
}
