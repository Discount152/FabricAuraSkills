package dev.granite.graniteskills.common.api.implementation;

import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.api.skill.XpRequirements;
import dev.granite.graniteskills.common.GraniteSkillsPlugin;

public class ApiXpRequirements implements XpRequirements {

    private final GraniteSkillsPlugin plugin;

    public ApiXpRequirements(GraniteSkillsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public int getXpRequired(Skill skill, int level) {
        return plugin.getXpRequirements().getXpRequired(skill, level);
    }

    @Override
    public int getDefaultXpRequired(int level) {
        return plugin.getXpRequirements().getDefaultXpRequired(level);
    }

}
