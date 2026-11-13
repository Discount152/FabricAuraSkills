package dev.granite.graniteskills.common.mana;

import dev.granite.graniteskills.api.mana.CustomManaAbility;
import dev.granite.graniteskills.api.mana.ManaAbilities;
import dev.granite.graniteskills.api.mana.ManaAbility;
import dev.granite.graniteskills.api.mana.ManaAbilityProvider;
import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import dev.granite.graniteskills.common.registry.Registry;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

/**
 * Registry for storing mana abilities and their properties.
 */
public class ManaAbilityRegistry extends Registry<ManaAbility, ManaAbilityProvider> {

    public ManaAbilityRegistry(GraniteSkillsPlugin plugin) {
        super(plugin, ManaAbility.class, ManaAbilityProvider.class);
        registerDefaults();
    }

    @Override
    public void registerDefaults() {
        for (ManaAbility manaAbility : ManaAbilities.values()) {
            this.register(manaAbility.getId(), manaAbility, plugin.getManaAbilityManager().getSupplier());
        }
    }

    public ConfigurationNode getDefinedConfig() throws SerializationException {
        ConfigurationNode root = CommentedConfigurationNode.root();
        for (ManaAbility checking : getValues()) {
            if (!(checking instanceof CustomManaAbility manaAbility)) {
                continue;
            }
            ConfigurationNode config = root.node("mana_abilities", checking.getId().toString());
            config.node("enabled").set(true);
            CustomManaAbility.Defined defined = manaAbility.getDefined();
            config.node("base_value").set(defined.getBaseValue());
            config.node("value_per_level").set(defined.getValuePerLevel());
            config.node("base_cooldown").set(defined.getBaseCooldown());
            config.node("cooldown_per_level").set(defined.getCooldownPerLevel());
            config.node("base_mana_cost").set(defined.getBaseManaCost());
            config.node("mana_cost_per_level").set(defined.getManaCostPerLevel());
            config.node("max_level").set(defined.getMaxLevel());
            config.node("unlock").set(defined.getUnlock());
            config.node("level_up").set(defined.getLevelUp());
        }
        return root;
    }

}
