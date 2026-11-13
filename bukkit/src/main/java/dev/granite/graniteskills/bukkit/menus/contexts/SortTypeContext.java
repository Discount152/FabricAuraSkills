package dev.granite.graniteskills.bukkit.menus.contexts;

import dev.aurelium.slate.context.ContextProvider;
import dev.granite.graniteskills.bukkit.menus.SourcesMenu.SortType;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class SortTypeContext implements ContextProvider<SortType> {

    @Override
    public Class<SortType> getType() {
        return SortType.class;
    }

    @Nullable
    @Override
    public SortType parse(String menuName, String s) {
        return SortType.valueOf(s.toUpperCase(Locale.ROOT));
    }

}
