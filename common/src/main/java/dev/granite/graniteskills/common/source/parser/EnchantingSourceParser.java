package dev.granite.graniteskills.common.source.parser;

import dev.granite.graniteskills.api.item.ItemFilter;
import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import dev.granite.graniteskills.common.source.ConfigurateSourceContext;
import dev.granite.graniteskills.common.source.type.EnchantingSource;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

public class EnchantingSourceParser extends SourceParser<EnchantingSource> {

    public EnchantingSourceParser(GraniteSkillsPlugin plugin) {
        super(plugin);
    }

    @Override
    public EnchantingSource parse(ConfigurationNode source, ConfigurateSourceContext context) throws SerializationException {
        ItemFilter item = context.required(source, "item").get(ItemFilter.class);
        String unit = source.node("unit").getString();

        return new EnchantingSource(plugin, context.parseValues(source), item, unit);
    }

}
