package dev.granite.graniteskills.common.source.type;

import dev.granite.graniteskills.api.item.ItemFilter;
import dev.granite.graniteskills.api.source.SourceValues;
import dev.granite.graniteskills.api.source.type.BrewingXpSource;
import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import dev.granite.graniteskills.common.source.Source;
import org.jetbrains.annotations.NotNull;

public class BrewingSource extends Source implements BrewingXpSource {

    private final ItemFilter ingredients;
    private final BrewTriggers trigger;

    public BrewingSource(GraniteSkillsPlugin plugin, SourceValues values, ItemFilter ingredients, BrewTriggers trigger) {
        super(plugin, values);
        this.ingredients = ingredients;
        this.trigger = trigger;
    }

    @Override
    public @NotNull ItemFilter getIngredients() {
        return ingredients;
    }

    @Override
    public @NotNull BrewTriggers getTrigger() {
        return trigger;
    }

}
