package dev.aurelium.auraskills.fabric.ability;

import dev.aurelium.auraskills.api.ability.Abilities;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;

public final class FabricEnduranceAbilities extends FabricAbilityImpl {

    public FabricEnduranceAbilities(FabricAuraSkills plugin) {
        super(plugin, Abilities.ANTI_HUNGER, Abilities.RUNNER, Abilities.GOLDEN_HEAL, Abilities.RECOVERY, Abilities.MEAL_STEAL);
    }
}
