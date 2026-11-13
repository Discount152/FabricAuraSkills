package dev.granite.graniteskills.common.ability;

import dev.granite.graniteskills.api.ability.Abilities;
import dev.granite.graniteskills.api.ability.Ability;
import dev.granite.graniteskills.api.ability.AbilityProvider;
import dev.granite.graniteskills.api.ability.CustomAbility;
import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import dev.granite.graniteskills.common.registry.Registry;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

/**
 * Registry for storing abilities and their properties.
 */
public class AbilityRegistry extends Registry<Ability, AbilityProvider> {

    public AbilityRegistry(GraniteSkillsPlugin plugin) {
        super(plugin, Ability.class, AbilityProvider.class);
        registerDefaults();
    }

    @Override
    public void registerDefaults() {
        for (Ability ability : Abilities.values()) {
            this.register(ability.getId(), ability, plugin.getAbilityManager().getSupplier());
        }
    }

    public ConfigurationNode getDefinedConfig() throws SerializationException {
        ConfigurationNode root = CommentedConfigurationNode.root();
        for (Ability ability : getValues()) {
            if (!(ability instanceof CustomAbility customAbility)) {
                continue;
            }
            ConfigurationNode abilityNode = root.node("abilities", ability.getId().toString());
            abilityNode.node("enabled").set(true);
            CustomAbility.Defined defined = customAbility.getDefined();
            abilityNode.node("base_value").set(defined.getBaseValue());
            abilityNode.node("value_per_level").set(defined.getValuePerLevel());
            abilityNode.node("unlock").set(defined.getUnlock());
            abilityNode.node("level_up").set(defined.getLevelUp());
            abilityNode.node("max_level").set(defined.getMaxLevel());
        }
        return root;
    }

}
