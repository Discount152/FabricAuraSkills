package dev.granite.graniteskills.common.source.parser;

import dev.granite.graniteskills.api.item.ItemFilter;
import dev.granite.graniteskills.api.source.type.BrewingXpSource;
import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import dev.granite.graniteskills.common.source.ConfigurateSourceContext;
import dev.granite.graniteskills.common.source.type.BrewingSource;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

public class BrewingSourceParser extends SourceParser<BrewingSource> {

    public BrewingSourceParser(GraniteSkillsPlugin plugin) {
        super(plugin);
    }

    @Override
    public BrewingSource parse(ConfigurationNode source, ConfigurateSourceContext context) throws SerializationException {
        ItemFilter ingredients = context.required(source, "ingredient").get(ItemFilter.class);
        BrewingXpSource.BrewTriggers trigger = context.required(source, "trigger").get(BrewingXpSource.BrewTriggers.class);

        return new BrewingSource(plugin, context.parseValues(source), ingredients, trigger);
    }

}
