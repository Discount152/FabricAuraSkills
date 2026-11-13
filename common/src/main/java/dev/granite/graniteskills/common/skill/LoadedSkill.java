package dev.granite.graniteskills.common.skill;

import com.google.common.collect.ImmutableList;
import dev.granite.graniteskills.api.ability.Ability;
import dev.granite.graniteskills.api.mana.ManaAbility;
import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.api.source.XpSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record LoadedSkill(Skill skill, @NotNull ImmutableList<Ability> abilities, @Nullable ManaAbility manaAbility,
        @NotNull ImmutableList<XpSource> sources, @NotNull SkillOptions options) {

}
