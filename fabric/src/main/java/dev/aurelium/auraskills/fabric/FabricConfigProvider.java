package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.common.config.ConfigProvider;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.Collections;
import java.util.Map;

public final class FabricConfigProvider extends ConfigProvider {

    public FabricConfigProvider(FabricAuraSkills plugin) {
        super(plugin, Collections.emptyMap());
    }

    @Override
    public void registerHooks(ConfigurationNode config) {
        // Optional Bukkit/Paper plugin hooks from the reference build are not wired on Fabric in the default product.
    }

    @Override
    public Object parseColor(String value) {
        return value.toUpperCase();
    }
}
