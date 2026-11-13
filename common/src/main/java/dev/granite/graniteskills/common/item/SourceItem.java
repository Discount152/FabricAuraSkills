package dev.granite.graniteskills.common.item;

import dev.granite.graniteskills.api.item.ItemCategory;
import dev.granite.graniteskills.api.item.ItemFilter;
import dev.granite.graniteskills.api.item.ItemFilterMeta;

public record SourceItem(String[] materials, String[] excludedMaterials, ItemCategory category,
        ItemFilterMeta meta) implements ItemFilter {

}
