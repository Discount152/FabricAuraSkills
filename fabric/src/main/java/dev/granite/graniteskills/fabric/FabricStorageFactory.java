package dev.granite.graniteskills.fabric;

import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import dev.granite.graniteskills.common.storage.StorageFactory;

public final class FabricStorageFactory extends StorageFactory {

    public FabricStorageFactory(GraniteSkillsPlugin plugin) {
        super(plugin);
    }

    @Override
    public String getDataDirectory() {
        return plugin.getPluginFolder().getPath() + "/userdata";
    }
}
