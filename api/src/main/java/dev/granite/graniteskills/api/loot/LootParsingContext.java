package dev.granite.graniteskills.api.loot;

import dev.granite.graniteskills.api.config.ConfigNode;

public interface LootParsingContext {

    LootValues parseValues(ConfigNode config, LootRequirements requirements);

}
