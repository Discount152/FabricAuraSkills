package dev.granite.graniteskills.fabric;

import dev.granite.graniteskills.common.util.text.TextUtil;

import java.util.List;

/**
 * First brewing source whose ingredient filter matches. A source that does not list the ingredient,
 * and a catch-all that excludes it, awards nothing for that source.
 */
public final class BrewingIngredientXp {

    public record Rule(String[] materials, String[] excluded, double xp) {
    }

    private BrewingIngredientXp() {
    }

    public static boolean matches(String itemPath, String[] materials, String[] excluded) {
        if (itemPath == null || itemPath.isEmpty()) {
            return false;
        }
        if (materials != null && !TextUtil.contains(materials, itemPath)) {
            return false;
        }
        if (excluded != null) {
            for (String excludedMaterial : excluded) {
                if (excludedMaterial != null && excludedMaterial.equalsIgnoreCase(itemPath)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static double xpForFirstMatch(String itemPath, List<Rule> rulesInOrder) {
        if (rulesInOrder == null) {
            return 0.0;
        }
        for (Rule rule : rulesInOrder) {
            if (rule != null && matches(itemPath, rule.materials(), rule.excluded())) {
                return rule.xp();
            }
        }
        return 0.0;
    }
}
