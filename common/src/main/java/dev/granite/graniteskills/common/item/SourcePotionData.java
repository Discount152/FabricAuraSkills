package dev.granite.graniteskills.common.item;

import dev.granite.graniteskills.api.item.PotionData;

public record SourcePotionData(String[] types, String[] excludedTypes, boolean extended, boolean upgraded,
        boolean excludeNegative) implements PotionData {

}
