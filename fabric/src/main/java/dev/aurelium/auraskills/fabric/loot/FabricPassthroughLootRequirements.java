package dev.aurelium.auraskills.fabric.loot;

import dev.aurelium.auraskills.api.loot.LootRequirements;

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
