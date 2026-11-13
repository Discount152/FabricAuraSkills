package dev.granite.graniteskills.common.reward;

import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import dev.granite.graniteskills.common.hooks.HookManager;
import dev.granite.graniteskills.common.user.User;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public abstract class SkillReward {

    protected final GraniteSkillsPlugin plugin;
    protected final HookManager hooks;
    private final Skill skill;

    public SkillReward(GraniteSkillsPlugin plugin, Skill skill) {
        this.plugin = plugin;
        this.hooks = plugin.getHookManager();
        this.skill = skill;
    }

    public Skill getSkill() {
        return skill;
    }

    public abstract void giveReward(User user, Skill skill, int level);

    @Nullable
    public abstract String getMenuMessage(User user, Locale locale, Skill skill, int level);

    @Nullable
    public abstract String getChatMessage(User user, Locale locale, Skill skill, int level);

}
