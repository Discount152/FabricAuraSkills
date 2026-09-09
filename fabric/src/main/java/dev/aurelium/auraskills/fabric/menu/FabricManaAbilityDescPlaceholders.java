package dev.aurelium.auraskills.fabric.menu;

import dev.aurelium.auraskills.api.mana.ManaAbilities;
import dev.aurelium.auraskills.api.mana.ManaAbility;
import dev.aurelium.auraskills.common.util.text.TextUtil;

/**
 * Mirrors Bukkit BukkitManaAbilityManager#getBaseDescription
 * provider-specific substitutions, so Fabric menu lore matches stock AuraSkills.
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