package dev.granite.graniteskills.common.source.type;

import dev.granite.graniteskills.api.item.ItemFilter;
import dev.granite.graniteskills.api.source.SourceValues;
import dev.granite.graniteskills.api.source.type.AnvilXpSource;
import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import dev.granite.graniteskills.common.source.Source;
import org.jetbrains.annotations.NotNull;

public class AnvilSource extends Source implements AnvilXpSource {

    private final ItemFilter leftItem;
    private final ItemFilter rightItem;
    private final String multiplier;

    public AnvilSource(GraniteSkillsPlugin plugin, SourceValues values, ItemFilter leftItem, ItemFilter rightItem, String multiplier) {
        super(plugin, values);
        this.leftItem = leftItem;
        this.rightItem = rightItem;
        this.multiplier = multiplier;
    }

    @Override
    public @NotNull ItemFilter getLeftItem() {
        return leftItem;
    }

    @Override
    public @NotNull ItemFilter getRightItem() {
        return rightItem;
    }

    @Override
    public String getMultiplier() {
        return multiplier;
    }

}
