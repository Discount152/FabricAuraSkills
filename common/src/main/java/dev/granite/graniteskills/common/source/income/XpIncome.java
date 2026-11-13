package dev.granite.graniteskills.common.source.income;

import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.api.source.SourceIncome;
import dev.granite.graniteskills.api.source.SourceValues;
import dev.granite.graniteskills.api.user.SkillsUser;
import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import dev.granite.graniteskills.common.config.Option;

public class XpIncome implements SourceIncome {

    private final GraniteSkillsPlugin plugin;
    private final double incomePerXp;

    public XpIncome(GraniteSkillsPlugin plugin, double incomePerXp) {
        this.plugin = plugin;
        this.incomePerXp = incomePerXp;
    }

    @Override
    public double getIncomeEarned(SkillsUser user, SourceValues sourceValues, Skill skill, double finalXp) {
        if (plugin.configBoolean(Option.JOBS_INCOME_USE_FINAL_XP)) {
            return incomePerXp * finalXp;
        } else {
            return incomePerXp * sourceValues.getXp();
        }
    }

}
