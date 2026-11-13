package dev.granite.graniteskills.common.util;

import dev.granite.graniteskills.common.config.Option;

import java.util.Map;

public record TestSession(Map<Option, Object> configOverrides) {

    public static TestSession create() {
        return new TestSession(Map.of());
    }

}
