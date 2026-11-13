package dev.granite.graniteskills.api.source;

import dev.granite.graniteskills.api.config.ConfigNode;

@FunctionalInterface
public interface UtilityParser<T> {

    T parse(ConfigNode source, BaseContext context);

}
