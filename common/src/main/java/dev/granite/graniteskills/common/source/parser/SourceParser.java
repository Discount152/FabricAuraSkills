package dev.granite.graniteskills.common.source.parser;

import dev.granite.graniteskills.api.config.ConfigNode;
import dev.granite.graniteskills.api.source.SourceContext;
import dev.granite.graniteskills.api.source.XpSourceParser;
import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import dev.granite.graniteskills.common.api.implementation.ApiConfigNode;
import dev.granite.graniteskills.common.source.ConfigurateSourceContext;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

public abstract class SourceParser<T> implements XpSourceParser<T> {

    protected final GraniteSkillsPlugin plugin;

    public SourceParser(GraniteSkillsPlugin plugin) {
        this.plugin = plugin;
    }

    public abstract T parse(ConfigurationNode source, ConfigurateSourceContext context) throws SerializationException;

    @Override
    public T parse(ConfigNode source, SourceContext context) {
        try {
            return parse(((ApiConfigNode) source).getBacking(), new ConfigurateSourceContext(context));
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        }
    }

}
