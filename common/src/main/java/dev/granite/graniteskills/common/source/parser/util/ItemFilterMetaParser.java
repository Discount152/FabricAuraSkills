package dev.granite.graniteskills.common.source.parser.util;

import dev.granite.graniteskills.api.config.ConfigNode;
import dev.granite.graniteskills.api.item.ItemFilterMeta;
import dev.granite.graniteskills.api.item.PotionData;
import dev.granite.graniteskills.api.source.BaseContext;
import dev.granite.graniteskills.api.source.UtilityParser;
import dev.granite.graniteskills.common.item.SourceItemMeta;

import java.util.List;

public class ItemFilterMetaParser implements UtilityParser<ItemFilterMeta> {

    @Override
    public ItemFilterMeta parse(ConfigNode source, BaseContext context) {
        String displayName = source.node("display_name").getString();
        List<String> lore = source.node("lore").getList(String.class);
        // Deserialize PotionData
        PotionData potionData;
        if (!source.node("potion_data").virtual()) {
            potionData = new PotionDataParser().parse(source.node("potion_data"), context);
        } else {
            potionData = null;
        }
        boolean hasCustomModelData = source.node("has_custom_model_data").getBoolean(false);
        if (!source.node("custom_model_data").virtual()) {
            hasCustomModelData = true;
        }
        int customModelData = source.node("custom_model_data").getInt(Integer.MIN_VALUE);
        boolean ignoreCustomModelData = source.node("ignore_custom_model_data").getBoolean(false);

        return new SourceItemMeta(displayName, lore, potionData, hasCustomModelData, customModelData, ignoreCustomModelData);
    }

}
