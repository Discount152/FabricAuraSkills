package dev.granite.graniteskills.common.ability;

import dev.granite.graniteskills.api.ability.Ability;
import dev.granite.graniteskills.api.skill.Skill;

public record LoadedAbility(Ability ability, Skill skill, AbilityConfig config) {

}
