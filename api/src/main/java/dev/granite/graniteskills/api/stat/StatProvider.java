package dev.granite.graniteskills.api.stat;

import dev.granite.graniteskills.api.option.OptionedProvider;
import dev.granite.graniteskills.api.trait.Trait;

import java.util.List;
import java.util.Locale;

public interface StatProvider extends OptionedProvider<Stat> {

    boolean isEnabled(Stat stat);

    List<Trait> getTraits(Stat stat);

    double getTraitModifier(Stat stat, Trait trait);

    String getDisplayName(Stat stat, Locale locale, boolean formatted);

    String getDescription(Stat stat, Locale locale, boolean formatted);

    String getColor(Stat stat, Locale locale);

    String getColoredName(Stat stat, Locale locale);

    String getSymbol(Stat stat, Locale locale);

}
