package dev.granite.graniteskills.common.item;

import dev.granite.graniteskills.api.item.ItemFilterMeta;
import dev.granite.graniteskills.api.item.PotionData;

import java.util.List;

public record SourceItemMeta(String displayName, List<String> lore, PotionData potionData, boolean hasCustomModelData,
        int customModelData, boolean ignoreCustomModelData) implements ItemFilterMeta {

}
