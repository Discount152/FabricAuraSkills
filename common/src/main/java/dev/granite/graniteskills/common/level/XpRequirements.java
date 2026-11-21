package dev.granite.graniteskills.common.level;

import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.common.GraniteSkillsPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manager for storing and retrieving xp requirements for each level.
 * Does not handle loading requirements from file.
 */
public class XpRequirements {

    private final GraniteSkillsPlugin plugin;
    private final List<Integer> defaultXpRequirements;
    private final Map<Skill, List<Integer>> skillXpRequirements;

    public XpRequirements(GraniteSkillsPlugin plugin) {
        this.plugin = plugin;
        this.defaultXpRequirements = new ArrayList<>();
        this.skillXpRequirements = new ConcurrentHashMap<>();
        addDefaultXpRequirements();
    }

    public void setSkillXpRequirements(Skill skill, List<Integer> xpRequirements) {
        skillXpRequirements.put(skill, xpRequirements);
    }

    public void removeSkillXpRequirements(Skill skill) {
        skillXpRequirements.remove(skill);
    }

    public void setDefaultXpRequirements(List<Integer> xpRequirements) {
        defaultXpRequirements.clear();
        defaultXpRequirements.addAll(xpRequirements);
    }

    public int getXpRequired(Skill skill, int level) {
        int index = level - plugin.config().getStartLevel() - 1;
        if (index < 0) {
            return 0;
        }
        List<Integer> skillList = skillXpRequirements.get(skill);
        if (skillList != null) {
            if (index < skillList.size()) {
                return skillList.get(index);
            } else {
                return 0;
            }
        }
        return getDefaultXpRequired(level);
    }

    public int getDefaultXpRequired(int level) {
        int index = level - plugin.config().getStartLevel() - 1;
        if (index >= 0 && index < defaultXpRequirements.size()) {
            return defaultXpRequirements.get(index);
        } else {
            return 0;
        }
    }

    public int getListSize(Skill skill) {
        List<Integer> skillList = skillXpRequirements.get(skill);
        if (skillList != null) {
            return skillList.size();
        }
        return defaultXpRequirements.size();
    }

    private void addDefaultXpRequirements() {
        defaultXpRequirements.clear();
        int highestMaxLevel = plugin.config().getHighestMaxLevel();
        for (int i = 0; i < highestMaxLevel - plugin.config().getStartLevel(); i++) {
            defaultXpRequirements.add(100 * i * i + 100);
        }
    }

}
