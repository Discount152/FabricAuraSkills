package dev.granite.graniteskills.common.loot;

import org.spongepowered.configurate.ConfigurationNode;

public interface CustomParser {

    boolean shouldUseParser(ConfigurationNode config);

}
