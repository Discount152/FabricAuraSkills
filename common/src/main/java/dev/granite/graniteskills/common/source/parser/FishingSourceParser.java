package dev.granite.graniteskills.common.source.parser;

import dev.granite.graniteskills.api.item.LootItemFilter;
import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import dev.granite.graniteskills.common.source.ConfigurateSourceContext;
import dev.granite.graniteskills.common.source.type.FishingSource;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

public class FishingSourceParser extends SourceParser<FishingSource> {

    public FishingSourceParser(GraniteSkillsPlugin plugin) {
        super(plugin);
    }

    @Override
    public FishingSource parse(ConfigurationNode source, ConfigurateSourceContext context) throws SerializationException {
        LootItemFilter item = context.required(source, "item").get(LootItemFilter.class);

        return new FishingSource(plugin, context.parseValues(source), item);
    }

}
