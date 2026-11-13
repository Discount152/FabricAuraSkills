package dev.granite.graniteskills.bukkit.ability;

import dev.granite.graniteskills.api.ability.Ability;
import dev.granite.graniteskills.api.ability.AbilityContext;
import dev.granite.graniteskills.bukkit.GraniteSkills;
import dev.granite.graniteskills.common.ability.AbilityImpl;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class BukkitAbilityImpl extends AbilityImpl implements Listener {

    protected final GraniteSkills plugin;
    private final AbilityContext abilityContext;

    public BukkitAbilityImpl(GraniteSkills plugin, Ability... abilities) {
        super(abilities);
        this.plugin = plugin;
        this.abilityContext = new AbilityContext(plugin.getApi());
    }

    protected boolean isDisabled(Ability ability) {
        return abilityContext.isDisabled(ability);
    }

    protected boolean failsChecks(Player player, Ability ability) {
        return abilityContext.failsChecks(player, ability);
    }

}
