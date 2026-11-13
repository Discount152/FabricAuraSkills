package dev.granite.graniteskills.common.user;

import dev.granite.graniteskills.api.skill.Skill;

import java.util.Map;

public record SkillLevelMaps(Map<Skill, Integer> levels, Map<Skill, Double> xp) {

}
