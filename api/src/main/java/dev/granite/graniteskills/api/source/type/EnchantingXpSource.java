package dev.granite.graniteskills.api.source.type;

import dev.granite.graniteskills.api.item.ItemFilter;
import dev.granite.graniteskills.api.source.XpSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface EnchantingXpSource extends XpSource {

    @NotNull
    ItemFilter getItem();

    @Nullable
    String getUnit();

}
