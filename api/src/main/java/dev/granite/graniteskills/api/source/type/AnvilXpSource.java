package dev.granite.graniteskills.api.source.type;

import dev.granite.graniteskills.api.item.ItemFilter;
import dev.granite.graniteskills.api.source.XpSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface AnvilXpSource extends XpSource {

    @NotNull
    ItemFilter getLeftItem();

    @NotNull
    ItemFilter getRightItem();

    @Nullable
    String getMultiplier();

}
