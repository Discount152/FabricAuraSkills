package dev.aurelium.auraskills.fabric.ability;

import dev.aurelium.auraskills.api.ability.Abilities;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;

public final class FabricAlchemyAbilities extends FabricAbilityImpl {

    public FabricAlchemyAbilities(FabricAuraSkills plugin) {
        super(plugin, Abilities.ALCHEMIST, Abilities.BREWER, Abilities.SPLASHER, Abilities.LINGERING, Abilities.WISE_EFFECT);
    }
}
