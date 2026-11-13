package dev.granite.graniteskills.fabric.ability;

import dev.granite.graniteskills.api.ability.Ability;
import dev.granite.graniteskills.common.ability.AbilityImpl;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import net.minecraft.server.level.ServerPlayer;

public abstract class FabricAbilityImpl extends AbilityImpl {

    protected final FabricGraniteSkills plugin;
    private final FabricAbilityContext abilityContext;

    protected FabricAbilityImpl(FabricGraniteSkills plugin, Ability... abilities) {
        super(abilities);
        this.plugin = plugin;
        this.abilityContext = new FabricAbilityContext(plugin);
    }

    protected boolean isDisabled(Ability ability) {
        return abilityContext.isDisabled(ability);
    }

    protected boolean failsChecks(ServerPlayer player, Ability ability) {
        return abilityContext.failsChecks(player, ability);
    }
}
