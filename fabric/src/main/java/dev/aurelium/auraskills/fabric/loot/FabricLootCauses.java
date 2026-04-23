package dev.aurelium.auraskills.fabric.loot;

import dev.aurelium.auraskills.api.loot.LootDropCause;

/**
 * Fabric-side {@link LootDropCause} markers for block loot (Bukkit {@code LootDropEvent.Cause} has the same roles;
 * api-bukkit is not on the Fabric classpath).
 */
public enum FabricLootCauses implements LootDropCause {
    METAL_DETECTOR,
    LUCKY_SPADES,
    EXCAVATION_OTHER_LOOT,
    MINING_OTHER_LOOT,
    FORAGING_OTHER_LOOT,
    FARMING_OTHER_LOOT,
    UNKNOWN
}
