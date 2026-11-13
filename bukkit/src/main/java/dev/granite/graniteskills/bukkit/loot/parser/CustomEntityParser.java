package dev.granite.graniteskills.bukkit.loot.parser;

import dev.granite.graniteskills.bukkit.loot.entity.EntitySupplier;
import dev.granite.graniteskills.common.loot.CustomParser;
import org.spongepowered.configurate.ConfigurationNode;

public interface CustomEntityParser extends CustomParser {

    EntitySupplier getEntitySupplier(ConfigurationNode config);

}
