package dev.granite.graniteskills.common.item;

import dev.granite.graniteskills.api.item.ItemCategory;
import dev.granite.graniteskills.api.item.ItemFilterMeta;
import dev.granite.graniteskills.api.item.LootItemFilter;

public record LootSourceItem(String[] materials, String[] excludedMaterials, ItemCategory category, ItemFilterMeta meta,
        String lootPool) implements LootItemFilter {

}
