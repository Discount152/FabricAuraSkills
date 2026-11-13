package dev.granite.graniteskills.api.loot;

import dev.granite.graniteskills.api.config.ConfigNode;

@FunctionalInterface
public interface LootParser {

    Loot parse(LootParsingContext context, ConfigNode config, LootRequirements requirements);

}
