package dev.aurelium.auraskills.fabric.loot;

import dev.aurelium.auraskills.api.config.ConfigNode;
import dev.aurelium.auraskills.api.loot.Loot;
import dev.aurelium.auraskills.api.loot.LootParser;
import dev.aurelium.auraskills.api.loot.LootParsingContext;
import dev.aurelium.auraskills.api.loot.LootRequirements;
import dev.aurelium.auraskills.common.api.implementation.ApiConfigNode;
import org.spongepowered.configurate.ConfigurationNode;

/**
 * Parses vanilla entity ids such as {@code minecraft:zombie}. Custom entity plugins are not required.
 */
public final class FabricEntityLootParser implements LootParser {

    @Override
    public Loot parse(LootParsingContext context, ConfigNode config, LootRequirements requirements) {
        ConfigurationNode backing = ((ApiConfigNode) config).getBacking();
        String entityType = backing.node("entity").getString();
        if (entityType == null || entityType.isBlank()) {
            throw new IllegalArgumentException("Entity loot must specify an entity type");
        }
        return new FabricEntityLoot(context.parseValues(config, requirements), entityType);
    }
}
