package dev.granite.graniteskills.common.reward.type;

import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.api.stat.Stat;
import dev.granite.graniteskills.api.trait.Traits;
import dev.granite.graniteskills.api.util.NumberUtil;
import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import dev.granite.graniteskills.common.message.type.LevelerFormat;
import dev.granite.graniteskills.common.reward.SkillReward;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.common.util.text.TextUtil;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class StatReward extends SkillReward {

    private final Stat stat;
    private final double value;
    private NumberFormat numberFormat;

    public StatReward(GraniteSkillsPlugin plugin, Skill skill, Stat stat, double value, @Nullable String format) {
        super(plugin, skill);
        this.stat = stat;
        this.value = value;
        if (format != null) {
            this.numberFormat = new DecimalFormat(format);
        }
    }

    @Override
    public void giveReward(User user, Skill skill, int level) {
        user.getUserStats().recalculateStat(stat);
        plugin.getStatManager().reload(user, stat);
    }

    public Stat getStat() {
        return stat;
    }

    public double getValue() {
        return value;
    }

    @Override
    public String getMenuMessage(User player, Locale locale, Skill skill, int level) {
        String format = plugin.getMenuHelper().getFormat("level_progression", "stat_reward_entry");
        return TextUtil.replace(format,
                "{color}", stat.getColor(locale),
                "{num}", getDisplayValue(locale),
                "{symbol}", stat.getSymbol(locale),
                "{stat}", stat.getDisplayName(locale, false));
    }

    @Override
    public String getChatMessage(User player, Locale locale, Skill skill, int level) {
        return TextUtil.replace(plugin.getMessageProvider().getRaw(LevelerFormat.STAT_LEVEL, locale),
                "{color}", stat.getColor(locale),
                "{num}", getDisplayValue(locale),
                "{symbol}", stat.getSymbol(locale),
                "{stat}", stat.getDisplayName(locale, false));
    }

    private String getDisplayValue(Locale locale) {
        if (stat.getTraits().contains(Traits.HP) && stat.getTraitModifier(Traits.HP) == 1.0) {
            return plugin.getTraitManager().getMenuDisplay(Traits.HP, value, locale, numberFormat);
        } else if (numberFormat != null) {
            return numberFormat.format(value);
        } else {
            return NumberUtil.format1(value);
        }
    }

}
