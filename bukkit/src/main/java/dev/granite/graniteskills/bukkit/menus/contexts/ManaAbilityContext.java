package dev.granite.graniteskills.bukkit.menus.contexts;

import dev.aurelium.slate.context.ContextProvider;
import dev.granite.graniteskills.api.mana.ManaAbility;
import dev.granite.graniteskills.api.registry.NamespacedId;
import dev.granite.graniteskills.bukkit.GraniteSkills;
import org.jetbrains.annotations.Nullable;

public class ManaAbilityContext implements ContextProvider<ManaAbility> {

    private final GraniteSkills plugin;

    public ManaAbilityContext(GraniteSkills plugin) {
        this.plugin = plugin;
    }

    @Override
    public Class<ManaAbility> getType() {
        return ManaAbility.class;
    }

    @Override
    @Nullable
    public ManaAbility parse(String menuName, String input) {
        ManaAbility manaAbility = plugin.getManaAbilityRegistry().getOrNull(NamespacedId.fromDefault(input));
        if (manaAbility != null && manaAbility.isEnabled()) {
            return manaAbility;
        } else {
            return null;
        }
    }

}
