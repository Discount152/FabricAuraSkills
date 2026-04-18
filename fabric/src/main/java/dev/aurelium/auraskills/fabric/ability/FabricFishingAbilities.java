package dev.aurelium.auraskills.fabric.ability;

import dev.aurelium.auraskills.api.ability.Abilities;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;

public final class FabricFishingAbilities extends FabricAbilityImpl {

    public FabricFishingAbilities(FabricAuraSkills plugin) {
        super(plugin, Abilities.LUCKY_CATCH, Abilities.FISHER, Abilities.TREASURE_HUNTER, Abilities.GRAPPLER, Abilities.EPIC_CATCH);
    }
}
