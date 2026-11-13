package dev.granite.graniteskills.api.source.type;

import dev.granite.graniteskills.api.item.LootItemFilter;
import dev.granite.graniteskills.api.source.XpSource;
import org.jetbrains.annotations.NotNull;

public interface FishingXpSource extends XpSource {

    /**
     * Gets the valid items of the source.
     *
     * @return The items
     */
    @NotNull
    LootItemFilter getItem();

}
