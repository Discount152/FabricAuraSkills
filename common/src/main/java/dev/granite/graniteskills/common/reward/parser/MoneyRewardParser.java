package dev.granite.graniteskills.common.reward.parser;

import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import dev.granite.graniteskills.common.reward.SkillReward;
import dev.granite.graniteskills.common.reward.builder.MoneyRewardBuilder;
import org.spongepowered.configurate.ConfigurationNode;

public class MoneyRewardParser extends RewardParser {

    public MoneyRewardParser(GraniteSkillsPlugin plugin, Skill skill) {
        super(plugin, skill);
    }

    @Override
    public SkillReward parse(ConfigurationNode config) {
        return new MoneyRewardBuilder(plugin)
                .amount(config.node("amount").getDouble())
                .formula(config.node("formula").getString())
                .skill(skill)
                .build();
    }

}
