package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.api.loot.LootManager;
import dev.aurelium.auraskills.common.api.implementation.ApiProvider;

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
