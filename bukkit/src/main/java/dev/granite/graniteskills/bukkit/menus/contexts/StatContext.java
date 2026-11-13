package dev.granite.graniteskills.bukkit.menus.contexts;

import dev.aurelium.slate.context.ContextProvider;
import dev.granite.graniteskills.api.registry.NamespacedId;
import dev.granite.graniteskills.api.stat.Stat;
import dev.granite.graniteskills.bukkit.GraniteSkills;
import org.jetbrains.annotations.Nullable;

public class StatContext implements ContextProvider<Stat> {

    private final GraniteSkills plugin;

    public StatContext(GraniteSkills plugin) {
        this.plugin = plugin;
    }

    @Override
    public Class<Stat> getType() {
        return Stat.class;
    }

    @Nullable
    @Override
    public Stat parse(String menuName, String input) {
        Stat stat = plugin.getStatRegistry().getOrNull(NamespacedId.fromDefault(input));
        if (stat != null && stat.isEnabled()) {
            return stat;
        } else {
            return null;
        }
    }

}
