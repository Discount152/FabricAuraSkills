package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.common.AuraSkillsPlugin;
import dev.aurelium.auraskills.common.storage.StorageFactory;

public final class FabricStorageFactory extends StorageFactory {

    public FabricStorageFactory(AuraSkillsPlugin plugin) {
        super(plugin);
    }

    @Override
    public String getDataDirectory() {
        return plugin.getPluginFolder().getPath() + "/userdata";
    }
}
