package dev.granite.graniteskills.common.mana;

import dev.granite.graniteskills.api.mana.ManaAbility;
import dev.granite.graniteskills.api.skill.Skill;

public record LoadedManaAbility(ManaAbility manaAbility, Skill skill, ManaAbilityConfig config) {

}
