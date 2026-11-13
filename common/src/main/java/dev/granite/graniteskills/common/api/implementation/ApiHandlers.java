package dev.granite.graniteskills.common.api.implementation;

import dev.granite.graniteskills.api.registry.Handlers;
import dev.granite.graniteskills.api.trait.TraitHandler;
import dev.granite.graniteskills.common.GraniteSkillsPlugin;

public class ApiHandlers implements Handlers {

    private final GraniteSkillsPlugin plugin;

    public ApiHandlers(GraniteSkillsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void registerTraitHandler(TraitHandler traitHandler) {
        plugin.getTraitManager().registerTraitHandler(traitHandler);
    }

}
