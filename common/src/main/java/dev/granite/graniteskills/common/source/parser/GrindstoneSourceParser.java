package dev.granite.graniteskills.common.source.parser;

import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import dev.granite.graniteskills.common.source.ConfigurateSourceContext;
import dev.granite.graniteskills.common.source.type.GrindstoneSource;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

public class GrindstoneSourceParser extends SourceParser<GrindstoneSource> {

    public GrindstoneSourceParser(GraniteSkillsPlugin plugin) {
        super(plugin);
    }

    @Override
    public GrindstoneSource parse(ConfigurationNode source, ConfigurateSourceContext context) throws SerializationException {
        String multiplier = source.node("multiplier").getString();

        return new GrindstoneSource(plugin, context.parseValues(source), multiplier);
    }

}
