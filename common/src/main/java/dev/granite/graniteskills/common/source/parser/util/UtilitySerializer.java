package dev.granite.graniteskills.common.source.parser.util;

import dev.granite.graniteskills.api.source.BaseContext;
import dev.granite.graniteskills.api.source.UtilityParser;
import dev.granite.graniteskills.common.api.implementation.ApiConfigNode;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;

public class UtilitySerializer<T> implements TypeSerializer<T> {

    private final UtilityParser<T> parser;
    private final BaseContext context;

    public UtilitySerializer(UtilityParser<T> parser, BaseContext context) {
        this.parser = parser;
        this.context = context;
    }

    @Override
    public T deserialize(Type type, ConfigurationNode node) {
        return parser.parse(ApiConfigNode.toApi(node), context);
    }

    @Override
    public void serialize(Type type, @Nullable T obj, ConfigurationNode node) throws SerializationException {
        // Empty because we don't need to serialize
    }

}
