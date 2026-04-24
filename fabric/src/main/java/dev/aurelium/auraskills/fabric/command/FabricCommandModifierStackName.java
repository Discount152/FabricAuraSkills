package dev.aurelium.auraskills.fabric.command;

import dev.aurelium.auraskills.api.util.NumberUtil;

import java.util.Set;

/**
 * Stack modifier names with {@code (n)} suffix, matching Bukkit {@code ModifierCommand#getStackedName}.
 */
public final class FabricCommandModifierStackName {

    private FabricCommandModifierStackName() {}

    public static String getStackedName(Set<String> modifierNames, String name) {
        int lastStackNumber = 1;
        for (String modifierName : modifierNames) {
            if (!modifierName.startsWith(name)) {
                continue;
            }
            String endName = modifierName.substring(name.length());
            if (endName.startsWith("(") && endName.endsWith(")")) {
                String numberString = endName.substring(1, endName.length() - 1);
                int stackNumber = NumberUtil.toInt(numberString);
                if (stackNumber > lastStackNumber) {
                    lastStackNumber = stackNumber;
                }
            }
        }
        return name + "(" + (lastStackNumber + 1) + ")";
    }
}
