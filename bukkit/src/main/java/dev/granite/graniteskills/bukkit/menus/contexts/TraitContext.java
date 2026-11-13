package dev.granite.graniteskills.bukkit.menus.contexts;

import dev.aurelium.slate.context.ContextProvider;
import dev.granite.graniteskills.api.registry.NamespacedId;
import dev.granite.graniteskills.api.trait.Trait;
import dev.granite.graniteskills.bukkit.GraniteSkills;
import org.jetbrains.annotations.Nullable;

public class TraitContext implements ContextProvider<Trait> {

    private final GraniteSkills plugin;

    public TraitContext(GraniteSkills plugin) {
        this.plugin = plugin;
    }

    @Override
    public Class<Trait> getType() {
        return Trait.class;
    }

    @Override
    @Nullable
    public Trait parse(String menuName, String input) {
        Trait trait = plugin.getTraitRegistry().getOrNull(NamespacedId.fromDefault(input));
        if (trait != null && trait.isEnabled()) {
            return trait;
        } else {
            return null;
        }
    }

}
