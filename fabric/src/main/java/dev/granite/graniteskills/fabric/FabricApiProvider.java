package dev.granite.graniteskills.fabric;

import dev.granite.graniteskills.api.loot.LootManager;
import dev.granite.graniteskills.common.api.implementation.ApiProvider;

public final class FabricApiProvider implements ApiProvider {

    private final LootManager lootManager;

    public FabricApiProvider(LootManager lootManager) {
        this.lootManager = lootManager;
    }

    @Override
    public LootManager getLootManager() {
        return lootManager;
    }
}
