package dev.granite.graniteskills.bukkit.loot.type;

import dev.granite.graniteskills.api.loot.Loot;
import dev.granite.graniteskills.api.loot.LootValues;
import dev.granite.graniteskills.bukkit.loot.entity.EntitySupplier;

public class EntityLoot extends Loot {

    private final EntitySupplier entity;

    public EntityLoot(LootValues values, EntitySupplier entity) {
        super(values);
        this.entity = entity;
    }

    public EntitySupplier getEntity() {
        return entity;
    }

}
