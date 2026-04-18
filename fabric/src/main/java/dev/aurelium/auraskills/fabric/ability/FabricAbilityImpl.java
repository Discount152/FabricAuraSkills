package dev.aurelium.auraskills.fabric.ability;

import dev.aurelium.auraskills.api.ability.Ability;
import dev.aurelium.auraskills.common.ability.AbilityImpl;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import net.minecraft.server.network.ServerPlayerEntity;

public abstract class FabricAbilityImpl extends AbilityImpl {

    protected final FabricAuraSkills plugin;
    private final FabricAbilityContext abilityContext;

    protected FabricAbilityImpl(FabricAuraSkills plugin, Ability... abilities) {
        super(abilities);
        this.plugin = plugin;
        this.abilityContext = new FabricAbilityContext(plugin);
    }

    protected boolean isDisabled(Ability ability) {
        return abilityContext.isDisabled(ability);
    }

    protected boolean failsChecks(ServerPlayerEntity player, Ability ability) {
        return abilityContext.failsChecks(player, ability);
    }
}
