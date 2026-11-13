package dev.granite.graniteskills.fabric.ability;

import dev.granite.graniteskills.api.ability.Abilities;
import dev.granite.graniteskills.api.damage.DamageModifier;
import dev.granite.graniteskills.api.damage.DamageType;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import dev.granite.graniteskills.fabric.damage.FabricDamageMeta;
import net.minecraft.server.level.ServerPlayer;

public final class FabricForagingAbilities extends FabricAbilityImpl implements FabricDamagePipelineContributor {

    public FabricForagingAbilities(FabricGraniteSkills plugin) {
        super(plugin, Abilities.LUMBERJACK, Abilities.FORAGER, Abilities.AXE_MASTER, Abilities.SHREDDER, Abilities.VALOR);
    }

    @Override
    public void contributeAttackDamage(FabricGraniteSkills plugin, FabricDamageMeta meta, ServerPlayer attacker) {
        if (meta.getDamageType() != DamageType.AXE) {
            return;
        }
        User user = plugin.getUserManager().getUser(attacker.getUUID());
        meta.addAttackModifier(axeMaster(attacker, user));
    }

    private DamageModifier axeMaster(ServerPlayer player, User user) {
        var ability = Abilities.AXE_MASTER;
        if (isDisabled(ability) || failsChecks(player, ability)) {
            return DamageModifier.none();
        }
        if (user.getAbilityLevel(ability) <= 0) {
            return DamageModifier.none();
        }
        return new DamageModifier(getValue(ability, user) / 100.0, DamageModifier.Operation.ADD_COMBINED);
    }
}
