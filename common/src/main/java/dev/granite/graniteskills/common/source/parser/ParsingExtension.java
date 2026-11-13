package dev.granite.graniteskills.common.source.parser;

import dev.granite.graniteskills.api.source.XpSource;

@FunctionalInterface
public interface ParsingExtension {

    XpSource parse(XpSource source);

}
