package dev.granite.graniteskills.common.source.type;

import dev.granite.graniteskills.api.item.ItemFilter;
import dev.granite.graniteskills.api.source.SourceValues;
import dev.granite.graniteskills.api.source.type.EnchantingXpSource;
import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import dev.granite.graniteskills.common.source.Source;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EnchantingSource extends Source implements EnchantingXpSource {

    private final ItemFilter item;
    private final String unit;

    public EnchantingSource(GraniteSkillsPlugin plugin, SourceValues values, ItemFilter item, String unit) {
        super(plugin, values);
        this.item = item;
        this.unit = unit;
    }

    @Override
    public @NotNull ItemFilter getItem() {
        return item;
    }

    @Override
    public @Nullable String getUnit() {
        return unit;
    }

}
