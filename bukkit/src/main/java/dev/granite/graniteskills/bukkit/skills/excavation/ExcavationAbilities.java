package dev.granite.graniteskills.bukkit.skills.excavation;

import dev.granite.graniteskills.api.ability.Abilities;
import dev.granite.graniteskills.api.damage.DamageModifier;
import dev.granite.graniteskills.api.damage.DamageType;
import dev.granite.graniteskills.api.event.damage.DamageEvent;
import dev.granite.graniteskills.bukkit.GraniteSkills;
import dev.granite.graniteskills.bukkit.ability.BukkitAbilityImpl;
import dev.granite.graniteskills.common.user.User;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

public class ExcavationAbilities extends BukkitAbilityImpl {

    public ExcavationAbilities(GraniteSkills plugin) {
        super(plugin, Abilities.METAL_DETECTOR, Abilities.EXCAVATOR, Abilities.SPADE_MASTER, Abilities.BIGGER_SCOOP, Abilities.LUCKY_SPADES);
    }

    private DamageModifier spadeMaster(Player player, User user) {
        var ability = Abilities.SPADE_MASTER;

        if (isDisabled(ability) || failsChecks(player, ability)) return DamageModifier.none();

        if (user.getAbilityLevel(ability) <= 0) return DamageModifier.none();

        return new DamageModifier(getValue(ability, user) / 100, DamageModifier.Operation.ADD_COMBINED);
    }

    @EventHandler(ignoreCancelled = true)
    public void damageListener(DamageEvent event) {
        var meta = event.getDamageMeta();
        var attacker = meta.getAttackerAsPlayer();

        if (attacker != null) {
            if (meta.getDamageType() == DamageType.SHOVEL) {
                var user = plugin.getUser(attacker);
                meta.addAttackModifier(spadeMaster(attacker, user));
            }
        }
    }

}
