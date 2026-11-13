package dev.aurelium.auraskills.fabric.loot;

import dev.aurelium.auraskills.api.loot.Loot;
import dev.aurelium.auraskills.api.loot.LootValues;

public final class FabricEntityLoot extends Loot {

    private final String entityId;

    public FabricEntityLoot(LootValues values, String entityId) {
        super(values);
        this.entityId = entityId;
    }

    public String getEntityId() {
        return entityId;
    }
}
