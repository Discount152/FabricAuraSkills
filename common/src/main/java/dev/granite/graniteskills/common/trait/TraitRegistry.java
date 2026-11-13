package dev.granite.graniteskills.common.trait;

import dev.granite.graniteskills.api.trait.CustomTrait;
import dev.granite.graniteskills.api.trait.Trait;
import dev.granite.graniteskills.api.trait.TraitProvider;
import dev.granite.graniteskills.api.trait.Traits;
import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import dev.granite.graniteskills.common.registry.Registry;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

public class TraitRegistry extends Registry<Trait, TraitProvider> {

    public TraitRegistry(GraniteSkillsPlugin plugin) {
        super(plugin, Trait.class, TraitProvider.class);
        registerDefaults();
    }

    @Override
    public void registerDefaults() {
        for (Trait trait : Traits.values()) {
            this.register(trait.getId(), trait, plugin.getTraitManager().getSupplier());
        }
    }

    public ConfigurationNode getDefinedConfig() throws SerializationException {
        ConfigurationNode root = CommentedConfigurationNode.root();
        for (Trait trait : getValues()) {
            if (!(trait instanceof CustomTrait)) {
                continue;
            }
            ConfigurationNode traitNode = root.node("traits", trait.getId().toString());
            traitNode.node("enabled").set(true);
        }
        return root;
    }

}
