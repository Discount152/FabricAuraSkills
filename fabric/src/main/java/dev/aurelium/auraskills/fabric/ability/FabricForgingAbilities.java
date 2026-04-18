package dev.aurelium.auraskills.fabric.ability;

import dev.aurelium.auraskills.api.ability.Abilities;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;

public final class FabricForgingAbilities extends FabricAbilityImpl {

    public FabricForgingAbilities(FabricAuraSkills plugin) {
        super(plugin, Abilities.DISENCHANTER, Abilities.FORGER, Abilities.REPAIRING, Abilities.ANVIL_MASTER, Abilities.SKILL_MENDER);
    }
}
