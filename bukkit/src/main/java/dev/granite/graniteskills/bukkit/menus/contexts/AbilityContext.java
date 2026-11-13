package dev.granite.graniteskills.bukkit.menus.contexts;

import dev.aurelium.slate.context.ContextProvider;
import dev.granite.graniteskills.api.ability.Ability;
import dev.granite.graniteskills.api.registry.NamespacedId;
import dev.granite.graniteskills.bukkit.GraniteSkills;
import org.jetbrains.annotations.Nullable;

public class AbilityContext implements ContextProvider<Ability> {

    private final GraniteSkills plugin;

    public AbilityContext(GraniteSkills plugin) {
        this.plugin = plugin;
    }

    @Override
    public Class<Ability> getType() {
        return Ability.class;
    }

    @Override
    @Nullable
    public Ability parse(String menuName, String input) {
        Ability ability = plugin.getAbilityRegistry().getOrNull(NamespacedId.fromDefault(input));
        if (ability != null && ability.isEnabled()) {
            return ability;
        } else {
            return null;
        }
    }

}
