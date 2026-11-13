package dev.granite.graniteskills.api.source.type;

import dev.granite.graniteskills.api.item.ItemFilter;
import dev.granite.graniteskills.api.source.XpSource;
import org.jetbrains.annotations.NotNull;

public interface BrewingXpSource extends XpSource {

    /**
     * Gets the valid ingredients of the source.
     *
     * @return The ingredients
     */
    @NotNull
    ItemFilter getIngredients();

    /**
     * Gets an array of triggers of the source.
     *
     * @return The triggers
     */
    @NotNull
    BrewTriggers getTrigger();

    enum BrewTriggers {

        BREW,
        TAKEOUT
    }

}
