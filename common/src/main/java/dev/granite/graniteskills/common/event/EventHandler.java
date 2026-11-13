package dev.granite.graniteskills.common.event;

import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.api.source.XpSource;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.common.util.data.Pair;
import org.jetbrains.annotations.Nullable;

public interface EventHandler {

    void callUserLoadEvent(User user);

    void callSkillLevelUpEvent(User user, Skill skill, int level);

    Pair<Boolean, Double> callXpGainEvent(User user, Skill skill, @Nullable XpSource source, double amount);

}
