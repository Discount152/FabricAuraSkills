package dev.granite.graniteskills.bukkit.requirement;

import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.bukkit.GraniteSkills;
import org.bukkit.entity.Player;

public class SkillNode extends RequirementNode {

    private final Skill skill;
    private final int level;

    public SkillNode(GraniteSkills plugin, Skill skill, int level, String message) {
        super(plugin, message);
        this.skill = skill;
        this.level = level;
    }

    @Override
    public boolean check(Player player) {
        return plugin.getUser(player).getSkillLevel(skill) >= level;
    }

}
