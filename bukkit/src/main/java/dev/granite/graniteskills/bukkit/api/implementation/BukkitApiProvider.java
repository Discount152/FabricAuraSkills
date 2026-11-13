package dev.granite.graniteskills.bukkit.api.implementation;

import dev.granite.graniteskills.api.loot.LootManager;
import dev.granite.graniteskills.bukkit.GraniteSkills;
import dev.granite.graniteskills.common.api.implementation.ApiProvider;

public class BukkitApiProvider implements ApiProvider {

    private final LootManager lootManager;

    public BukkitApiProvider(GraniteSkills plugin) {
        this.lootManager = new ApiLootManager(plugin);
    }

    @Override
    public LootManager getLootManager() {
        return lootManager;
    }

}
