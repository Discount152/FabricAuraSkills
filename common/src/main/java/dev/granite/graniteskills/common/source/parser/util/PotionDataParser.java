package dev.granite.graniteskills.common.source.parser.util;

import dev.granite.graniteskills.api.config.ConfigNode;
import dev.granite.graniteskills.api.item.PotionData;
import dev.granite.graniteskills.api.source.BaseContext;
import dev.granite.graniteskills.api.source.UtilityParser;
import dev.granite.graniteskills.common.item.SourcePotionData;

public class PotionDataParser implements UtilityParser<PotionData> {

    @Override
    public PotionData parse(ConfigNode source, BaseContext context) {
        String[] types = context.pluralizedArray("type", source, String.class);
        String[] excludedTypes = context.pluralizedArray("excluded_type", source, String.class);
        boolean extended = source.node("extended").getBoolean(false);
        boolean upgraded = source.node("upgraded").getBoolean(false);
        boolean excludeNegative = source.node("exclude_negative").getBoolean(false);

        return new SourcePotionData(types, excludedTypes, extended, upgraded, excludeNegative);
    }

}
