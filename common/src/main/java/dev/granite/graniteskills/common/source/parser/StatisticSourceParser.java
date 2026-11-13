package dev.granite.graniteskills.common.source.parser;

import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import dev.granite.graniteskills.common.source.ConfigurateSourceContext;
import dev.granite.graniteskills.common.source.type.StatisticSource;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

public class StatisticSourceParser extends SourceParser<StatisticSource> {

    public StatisticSourceParser(GraniteSkillsPlugin plugin) {
        super(plugin);
    }

    @Override
    public StatisticSource parse(ConfigurationNode source, ConfigurateSourceContext context) throws SerializationException {
        String statistic = context.required(source, "statistic").getString();
        double multiplier = source.node("multiplier").getDouble(1.0);
        int minimumIncrease = source.node("minimum_increase").getInt(1);

        return new StatisticSource(plugin, context.parseValues(source), statistic, multiplier, minimumIncrease);
    }

}
