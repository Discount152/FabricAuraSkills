package dev.granite.graniteskills.api.source;

import dev.granite.graniteskills.api.config.ConfigNode;

@FunctionalInterface
public interface XpSourceParser<T> {

    T parse(ConfigNode source, SourceContext context);

}
