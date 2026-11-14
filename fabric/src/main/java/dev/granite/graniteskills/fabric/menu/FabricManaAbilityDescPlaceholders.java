package dev.granite.graniteskills.fabric.menu;

import dev.granite.graniteskills.api.mana.ManaAbilities;
import dev.granite.graniteskills.api.mana.ManaAbility;
import dev.granite.graniteskills.common.util.text.TextUtil;

/**
 * Mirrors Bukkit BukkitManaAbilityManager#getBaseDescription
 * provider-specific substitutions, so Fabric menu lore matches stock GraniteSkills.
 */
public final class FabricManaAbilityDescPlaceholders {

    private FabricManaAbilityDescPlaceholders() {}

    public static String applyAbilitySpecificStatic(ManaAbility manaAbility, String input) {
        if (manaAbility == ManaAbilities.SPEED_MINE) {
            return TextUtil.replace(
                    input,
                    "{haste_level}",
                    String.valueOf(manaAbility.optionInt("haste_level", 10)));
        }
        if (manaAbility == ManaAbilities.TERRAFORM) {
            return TextUtil.replace(input, "{radius}", "4");
        }
        return input;
    }
}
