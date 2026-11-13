package dev.granite.graniteskills.fabric.loot;

import dev.granite.graniteskills.api.loot.LootRequirements;

import java.util.UUID;

/**
 * Fabric has no Bukkit-style loot requirement nodes yet; treat all loot requirements as satisfied so tables still load.
 */
public final class FabricPassthroughLootRequirements extends LootRequirements {

    @Override
    public boolean checkByUuid(UUID uuid) {
        return true;
    }
}
