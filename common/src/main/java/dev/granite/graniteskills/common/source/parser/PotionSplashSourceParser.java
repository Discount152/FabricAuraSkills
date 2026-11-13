package dev.granite.graniteskills.common.source.parser;

import dev.granite.graniteskills.api.item.ItemFilter;
import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import dev.granite.graniteskills.common.source.ConfigurateSourceContext;
import dev.granite.graniteskills.common.source.type.PotionSplashSource;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

public class PotionSplashSourceParser extends SourceParser<PotionSplashSource> {

    public PotionSplashSourceParser(GraniteSkillsPlugin plugin) {
        super(plugin);
    }

    @Override
    public PotionSplashSource parse(ConfigurationNode source, ConfigurateSourceContext context) throws SerializationException {
        ItemFilter item = context.required(source, "item").get(ItemFilter.class);

        return new PotionSplashSource(plugin, context.parseValues(source), item);
    }

}
