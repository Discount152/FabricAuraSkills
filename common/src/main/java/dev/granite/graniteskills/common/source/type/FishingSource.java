package dev.granite.graniteskills.common.source.type;

import dev.granite.graniteskills.api.item.LootItemFilter;
import dev.granite.graniteskills.api.source.SourceValues;
import dev.granite.graniteskills.api.source.type.FishingXpSource;
import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import dev.granite.graniteskills.common.source.Source;
import org.jetbrains.annotations.NotNull;

public class FishingSource extends Source implements FishingXpSource {

    private final LootItemFilter item;

    public FishingSource(GraniteSkillsPlugin plugin, SourceValues values, LootItemFilter item) {
        super(plugin, values);
        this.item = item;
    }

    @Override
    public @NotNull LootItemFilter getItem() {
        return item;
    }

}
