package dev.granite.graniteskills.common.reward.parser;

import dev.granite.graniteskills.api.registry.NamespacedId;
import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.api.stat.Stat;
import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import dev.granite.graniteskills.common.reward.SkillReward;
import dev.granite.graniteskills.common.reward.builder.StatRewardBuilder;
import org.spongepowered.configurate.ConfigurationNode;

public class StatRewardParser extends RewardParser {

    public StatRewardParser(GraniteSkillsPlugin plugin, Skill skill) {
        super(plugin, skill);
    }

    @Override
    public SkillReward parse(ConfigurationNode config) {
        String statName = config.node("stat").getString("");
        Stat stat = plugin.getStatRegistry().getOrNull(NamespacedId.fromDefault(statName));
        if (stat == null) {
            throw new IllegalArgumentException("Unknown stat with name: " + statName);
        }
        // Don't add reward for disabled stats
        if (!stat.isEnabled()) {
            return null;
        }

        return new StatRewardBuilder(plugin)
                .stat(stat)
                .value(config.node("value").getDouble())
                .format(config.node("format").getString())
                .skill(skill)
                .build();
    }

}
