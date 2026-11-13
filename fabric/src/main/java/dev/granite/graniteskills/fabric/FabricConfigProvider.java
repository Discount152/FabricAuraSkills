package dev.granite.graniteskills.fabric;

import dev.granite.graniteskills.common.config.ConfigProvider;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.Collections;

public final class FabricConfigProvider extends ConfigProvider {

    public FabricConfigProvider(FabricGraniteSkills plugin) {
        super(plugin, Collections.emptyMap());
    }

    @Override
    public void registerHooks(ConfigurationNode config) {
        // Paper plugin hooks (WorldGuard, Vault, etc.) are not wired on Fabric in the MVP.
    }

    @Override
    public Object parseColor(String value) {
        return value.toUpperCase();
    }
}
