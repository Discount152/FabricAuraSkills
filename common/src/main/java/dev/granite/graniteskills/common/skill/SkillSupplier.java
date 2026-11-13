package dev.granite.graniteskills.common.skill;

import com.google.common.collect.ImmutableList;
import dev.granite.graniteskills.api.ability.Ability;
import dev.granite.graniteskills.api.mana.ManaAbility;
import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.api.skill.SkillProvider;
import dev.granite.graniteskills.api.source.XpSource;
import dev.granite.graniteskills.common.message.MessageProvider;
import dev.granite.graniteskills.common.registry.OptionSupplier;
import dev.granite.graniteskills.common.util.data.OptionProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class SkillSupplier extends OptionSupplier<Skill> implements SkillProvider {

    private final SkillManager skillManager;
    private final MessageProvider messageProvider;

    public SkillSupplier(SkillManager skillManager, MessageProvider messageProvider) {
        this.skillManager = skillManager;
        this.messageProvider = messageProvider;
    }

    private LoadedSkill get(Skill skill) {
        return this.skillManager.getSkill(skill);
    }

    @Override
    public boolean isEnabled(Skill skill) {
        if (!skillManager.isLoaded(skill)) {
            return false;
        }
        return get(skill).options().enabled();
    }

    @Override
    public @NotNull ImmutableList<Ability> getAbilities(Skill skill) {
        return get(skill).abilities();
    }

    @Override
    public @Nullable ManaAbility getManaAbility(Skill skill) {
        return get(skill).manaAbility();
    }

    @Override
    public @NotNull ImmutableList<XpSource> getSources(Skill skill) {
        return get(skill).sources();
    }

    @Override
    public int getMaxLevel(Skill skill) {
        return get(skill).options().maxLevel();
    }

    @Override
    public String getDisplayName(Skill skill, Locale locale, boolean formatted) {
        return messageProvider.getSkillDisplayName(skill, locale, formatted);
    }

    @Override
    public String getDescription(Skill skill, Locale locale, boolean formatted) {
        return messageProvider.getSkillDescription(skill, locale, formatted);
    }

    @Override
    public OptionProvider getOptions(Skill type) {
        return get(type).options();
    }

    @Override
    public boolean isLoaded(Skill type) {
        return skillManager.isLoaded(type);
    }

}
