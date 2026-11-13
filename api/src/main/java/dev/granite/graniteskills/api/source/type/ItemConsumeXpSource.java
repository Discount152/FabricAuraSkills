package dev.granite.graniteskills.api.source.type;

import dev.granite.graniteskills.api.item.ItemFilter;
import dev.granite.graniteskills.api.source.XpSource;
import org.jetbrains.annotations.NotNull;

public interface ItemConsumeXpSource extends XpSource {

    @NotNull
    ItemFilter getItem();

}
