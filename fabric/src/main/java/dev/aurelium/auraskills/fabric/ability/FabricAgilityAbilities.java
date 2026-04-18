package dev.aurelium.auraskills.fabric.ability;

import dev.aurelium.auraskills.api.ability.Abilities;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;

public final class FabricAgilityAbilities extends FabricAbilityImpl {

    public FabricAgilityAbilities(FabricAuraSkills plugin) {
        super(plugin, Abilities.LIGHT_FALL, Abilities.JUMPER, Abilities.SUGAR_RUSH, Abilities.FLEETING, Abilities.THUNDER_FALL);
    }
}
