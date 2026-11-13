package dev.granite.graniteskills.common.source.parser;

import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import dev.granite.graniteskills.common.source.ConfigurateSourceContext;
import dev.granite.graniteskills.common.source.type.JumpingSource;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

public class JumpingSourceParser extends SourceParser<JumpingSource> {

    public JumpingSourceParser(GraniteSkillsPlugin plugin) {
        super(plugin);
    }

    @Override
    public JumpingSource parse(ConfigurationNode source, ConfigurateSourceContext context) throws SerializationException {
        int interval = source.node("interval").getInt(100);

        return new JumpingSource(plugin, context.parseValues(source), interval);
    }

}
