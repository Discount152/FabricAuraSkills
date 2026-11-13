package dev.granite.graniteskills.common.api.implementation;

import dev.granite.graniteskills.api.ability.Ability;
import dev.granite.graniteskills.api.mana.ManaAbility;
import dev.granite.graniteskills.api.registry.GlobalRegistry;
import dev.granite.graniteskills.api.registry.NamespacedId;
import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.api.stat.Stat;
import dev.granite.graniteskills.api.trait.Trait;
import dev.granite.graniteskills.common.GraniteSkillsPlugin;

import java.util.Collection;

public class ApiGlobalRegistry implements GlobalRegistry {

    private final GraniteSkillsPlugin plugin;

    public ApiGlobalRegistry(GraniteSkillsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Skill getSkill(NamespacedId id) {
        return plugin.getSkillRegistry().getOrNull(id);
    }

    @Override
    public Collection<Skill> getSkills() {
        return plugin.getSkillRegistry().getValues();
    }

    @Override
    public Stat getStat(NamespacedId id) {
        return plugin.getStatRegistry().getOrNull(id);
    }

    @Override
    public Collection<Stat> getStats() {
        return plugin.getStatRegistry().getValues();
    }

    @Override
    public Trait getTrait(NamespacedId id) {
        return plugin.getTraitRegistry().getOrNull(id);
    }

    @Override
    public Collection<Trait> getTraits() {
        return plugin.getTraitRegistry().getValues();
    }

    @Override
    public Ability getAbility(NamespacedId id) {
        return plugin.getAbilityRegistry().getOrNull(id);
    }

    @Override
    public Collection<Ability> getAbilities() {
        return plugin.getAbilityRegistry().getValues();
    }

    @Override
    public ManaAbility getManaAbility(NamespacedId id) {
        return plugin.getManaAbilityRegistry().getOrNull(id);
    }

    @Override
    public Collection<ManaAbility> getManaAbilities() {
        return plugin.getManaAbilityRegistry().getValues();
    }

}
