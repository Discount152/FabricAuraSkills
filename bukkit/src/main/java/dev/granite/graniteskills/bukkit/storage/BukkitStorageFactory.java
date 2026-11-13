package dev.granite.graniteskills.bukkit.storage;

import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import dev.granite.graniteskills.common.storage.StorageFactory;

public class BukkitStorageFactory extends StorageFactory {

    public BukkitStorageFactory(GraniteSkillsPlugin plugin) {
        super(plugin);
    }

    @Override
    public String getDataDirectory() {
        return plugin.getPluginFolder().getPath() + "/userdata";
    }

}
