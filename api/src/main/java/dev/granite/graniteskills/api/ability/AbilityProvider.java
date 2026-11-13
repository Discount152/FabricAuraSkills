package dev.granite.graniteskills.api.ability;

import dev.granite.graniteskills.api.option.OptionedProvider;
import dev.granite.graniteskills.api.skill.Skill;

import java.util.Locale;

public interface AbilityProvider extends OptionedProvider<Ability> {

    Skill getSkill(Ability ability);

    String getDisplayName(Ability ability, Locale locale, boolean formatted);

    String getDescription(Ability ability, Locale locale, boolean formatted);

    String getInfo(Ability ability, Locale locale, boolean formatted);

    boolean isEnabled(Ability ability);

    double getBaseValue(Ability ability);

    double getSecondaryBaseValue(Ability ability);

    double getValue(Ability ability, int level);

    double getValuePerLevel(Ability ability);

    double getSecondaryValuePerLevel(Ability ability);

    double getSecondaryValue(Ability ability, int level);

    int getUnlock(Ability ability);

    int getLevelUp(Ability ability);

    int getMaxLevel(Ability ability);

}
