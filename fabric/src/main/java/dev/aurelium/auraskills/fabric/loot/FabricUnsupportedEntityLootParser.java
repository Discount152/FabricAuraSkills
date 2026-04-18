package dev.aurelium.auraskills.fabric.loot;

import dev.aurelium.auraskills.api.config.ConfigNode;
import dev.aurelium.auraskills.api.loot.Loot;
import dev.aurelium.auraskills.api.loot.LootParser;
import dev.aurelium.auraskills.api.loot.LootParsingContext;
import dev.aurelium.auraskills.api.loot.LootRequirements;

/**
 * Placeholder until Fabric can resolve entity loot suppliers (Bukkit {@code EntityLootParser}).
 */
public final class FabricUnsupportedEntityLootParser implements LootParser {

    @Override
    public Loot parse(LootParsingContext context, ConfigNode config, LootRequirements requirements) {
        throw new UnsupportedOperationException("Entity loot type is not supported on Fabric yet");
    }
}
