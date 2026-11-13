package dev.granite.graniteskills.common.reward.builder;

import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import dev.granite.graniteskills.common.reward.SkillReward;
import dev.granite.graniteskills.common.reward.type.MoneyReward;

public class MoneyRewardBuilder extends RewardBuilder {

    private double amount;
    private String formula;

    public MoneyRewardBuilder(GraniteSkillsPlugin plugin) {
        super(plugin);
        this.formula = null;
    }

    public MoneyRewardBuilder amount(double amount) {
        this.amount = amount;
        return this;
    }

    public MoneyRewardBuilder formula(String formula) {
        this.formula = formula;
        return this;
    }

    @Override
    public SkillReward build() {
        return new MoneyReward(plugin, skill, amount, formula);
    }

}
