package dev.granite.graniteskills.common.reward.parser;

import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import dev.granite.graniteskills.common.reward.SkillReward;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;

public abstract class RewardParser {

    protected final GraniteSkillsPlugin plugin;
    protected final Skill skill;

    public RewardParser(GraniteSkillsPlugin plugin, Skill skill) {
        this.plugin = plugin;
        this.skill = skill;
    }

    @Nullable
    public abstract SkillReward parse(ConfigurationNode config);

}
