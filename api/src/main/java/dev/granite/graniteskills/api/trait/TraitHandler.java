package dev.granite.graniteskills.api.trait;

import dev.granite.graniteskills.api.util.NumberUtil;

import java.util.Locale;

public interface TraitHandler {

    Trait[] getTraits();

    default String getMenuDisplay(double value, Trait trait, Locale locale) {
        return NumberUtil.format1(value);
    }

    /**
     * Whether the value of {@link #getMenuDisplay(double, Trait, Locale)} has the same numerical value as the
     * actual trait level (ignoring any extra formatting like percent signs).
     *
     * @return whether the display value matches the trait value
     */
    default boolean displayMatchesValue() {
        return true;
    }

}
