package dev.granite.graniteskills.common.source.income;

import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.api.source.SourceIncome;
import dev.granite.graniteskills.api.source.SourceValues;
import dev.granite.graniteskills.api.user.SkillsUser;

public class FixedIncome implements SourceIncome {

    private final double income;

    public FixedIncome(double income) {
        this.income = income;
    }

    @Override
    public double getIncomeEarned(SkillsUser user, SourceValues sourceValues, Skill skill, double finalXp) {
        return income;
    }

}
