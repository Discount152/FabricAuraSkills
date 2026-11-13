package dev.granite.graniteskills.fabric.loot;

import dev.granite.graniteskills.api.loot.Loot;
import dev.granite.graniteskills.api.loot.LootValues;

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
