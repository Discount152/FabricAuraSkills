package dev.granite.graniteskills.common.source.type;

import dev.granite.graniteskills.api.item.ItemFilter;
import dev.granite.graniteskills.api.source.SourceValues;
import dev.granite.graniteskills.api.source.type.ItemConsumeXpSource;
import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import dev.granite.graniteskills.common.source.Source;
import org.jetbrains.annotations.NotNull;

public class ItemConsumeSource extends Source implements ItemConsumeXpSource {

    private final ItemFilter item;

    public ItemConsumeSource(GraniteSkillsPlugin plugin, SourceValues values, ItemFilter item) {
        super(plugin, values);
        this.item = item;
    }

    @Override
    public @NotNull ItemFilter getItem() {
        return item;
    }

}
