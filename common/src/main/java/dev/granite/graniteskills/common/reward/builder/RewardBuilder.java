package dev.granite.graniteskills.common.reward.builder;

import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import dev.granite.graniteskills.common.reward.SkillReward;

public abstract class RewardBuilder {

    protected final GraniteSkillsPlugin plugin;
    protected Skill skill;

    public RewardBuilder(GraniteSkillsPlugin plugin) {
        this.plugin = plugin;
    }

    public RewardBuilder skill(Skill skill) {
        this.skill = skill;
        return this;
    }

    public abstract SkillReward build();

}
