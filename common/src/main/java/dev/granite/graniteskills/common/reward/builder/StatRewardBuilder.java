package dev.granite.graniteskills.common.reward.builder;

import dev.granite.graniteskills.api.stat.Stat;
import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import dev.granite.graniteskills.common.reward.SkillReward;
import dev.granite.graniteskills.common.reward.type.StatReward;
import dev.granite.graniteskills.common.util.data.Validate;
import org.jetbrains.annotations.Nullable;

public class StatRewardBuilder extends RewardBuilder {

    private Stat stat;
    private double value = 1.0;
    private String format;

    public StatRewardBuilder(GraniteSkillsPlugin plugin) {
        super(plugin);
    }

    public StatRewardBuilder stat(Stat stat) {
        this.stat = stat;
        return this;
    }

    public StatRewardBuilder value(double value) {
        this.value = value;
        return this;
    }

    public StatRewardBuilder format(@Nullable String format) {
        this.format = format;
        return this;
    }

    @Override
    public SkillReward build() {
        Validate.notNull(stat, "You must specify a stat");
        return new StatReward(plugin, skill, stat, value, format);
    }

}
