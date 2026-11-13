package dev.granite.graniteskills.bukkit.loot.parser;

import dev.granite.graniteskills.api.config.ConfigNode;
import dev.granite.graniteskills.api.loot.Loot;
import dev.granite.graniteskills.api.loot.LootParser;
import dev.granite.graniteskills.api.loot.LootParsingContext;
import dev.granite.graniteskills.api.loot.LootRequirements;
import dev.granite.graniteskills.bukkit.loot.BukkitLootManager;
import dev.granite.graniteskills.bukkit.loot.entity.EntitySupplier;
import dev.granite.graniteskills.bukkit.loot.type.EntityLoot;
import dev.granite.graniteskills.common.api.implementation.ApiConfigNode;
import dev.granite.graniteskills.common.util.data.Validate;
import org.spongepowered.configurate.ConfigurationNode;

public class EntityLootParser implements LootParser {

    protected final BukkitLootManager manager;

    public EntityLootParser(BukkitLootManager manager) {
        this.manager = manager;
    }

    @Override
    public Loot parse(LootParsingContext context, ConfigNode config, LootRequirements requirements) {
        ConfigurationNode backing = ((ApiConfigNode) config).getBacking();

        String entityType = backing.node("entity").getString();
        Validate.notNull(entityType, "Entity loot must specify an entity type");

        EntitySupplier entity = null;
        for (CustomEntityParser parser : manager.getCustomEntityParsers()) {
            if (parser.shouldUseParser(backing)) {
                entity = parser.getEntitySupplier(backing);
                break;
            }
        }

        Validate.notNull(entity, "Couldn't parse entity loot with entity type: " + entityType);

        return new EntityLoot(context.parseValues(config, requirements), entity);
    }

}
