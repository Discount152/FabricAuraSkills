package dev.granite.graniteskills.bukkit.hooks.mythicmobs.loot;

import dev.granite.graniteskills.bukkit.GraniteSkills;
import dev.granite.graniteskills.bukkit.loot.entity.EntityProperties;
import dev.granite.graniteskills.bukkit.loot.entity.EntitySupplier;
import dev.granite.graniteskills.bukkit.loot.parser.CustomEntityParser;
import org.spongepowered.configurate.ConfigurationNode;

public class MythicEntityLootParser implements CustomEntityParser {

    private final GraniteSkills plugin;

    public MythicEntityLootParser(GraniteSkills plugin) {
        this.plugin = plugin;
    }

    @Override
    public EntitySupplier getEntitySupplier(ConfigurationNode config) {
        return new MythicEntitySupplier(EntityProperties.fromConfig(config, plugin));
    }

    @Override
    public boolean shouldUseParser(ConfigurationNode config) {
        return config.node("entity").getString("").startsWith("mythicmobs:");
    }

}
