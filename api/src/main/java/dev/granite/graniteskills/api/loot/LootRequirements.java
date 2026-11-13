package dev.granite.graniteskills.api.loot;

import java.util.UUID;

public abstract class LootRequirements {

    public abstract boolean checkByUuid(UUID uuid);

}
