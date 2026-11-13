package dev.granite.graniteskills.common.source.parser.util;

import dev.granite.graniteskills.api.config.ConfigNode;
import dev.granite.graniteskills.api.item.ItemCategory;
import dev.granite.graniteskills.api.item.ItemFilterMeta;
import dev.granite.graniteskills.api.item.LootItemFilter;
import dev.granite.graniteskills.api.source.BaseContext;
import dev.granite.graniteskills.api.source.UtilityParser;
import dev.granite.graniteskills.common.item.LootSourceItem;

public class LootItemFilterParser implements UtilityParser<LootItemFilter> {

    @Override
    public LootItemFilter parse(ConfigNode source, BaseContext context) {
        String[] materials = context.pluralizedArray("material", source, String.class);
        String[] excludedMaterials = context.pluralizedArray("excluded_material", source, String.class);
        ItemCategory category = source.node("category").get(ItemCategory.class);
        String lootPool = source.node("loot_pool").getString();
        ItemFilterMeta meta = new ItemFilterMetaParser().parse(source, context);

        return new LootSourceItem(materials, excludedMaterials, category, meta, lootPool);
    }

}
