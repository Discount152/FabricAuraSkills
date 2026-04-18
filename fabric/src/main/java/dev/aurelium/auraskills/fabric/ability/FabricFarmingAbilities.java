package dev.aurelium.auraskills.fabric.ability;

import dev.aurelium.auraskills.api.ability.Abilities;
import dev.aurelium.auraskills.api.damage.DamageModifier;
import dev.aurelium.auraskills.api.damage.DamageType;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.damage.FabricDamageMeta;
import net.minecraft.server.network.ServerPlayerEntity;

public final class FabricFarmingAbilities extends FabricAbilityImpl implements FabricDamagePipelineContributor {

    public FabricFarmingAbilities(FabricAuraSkills plugin) {
        super(plugin, Abilities.BOUNTIFUL_HARVEST, Abilities.FARMER, Abilities.GENETICIST, Abilities.SCYTHE_MASTER, Abilities.GROWTH_AURA);
    }

    @Override
    public void contributeAttackDamage(FabricAuraSkills plugin, FabricDamageMeta meta, ServerPlayerEntity attacker) {
        if (meta.getDamageType() != DamageType.HOE) {
            return;
        }
        User user = plugin.getUserManager().getUser(attacker.getUuid());
        meta.addAttackModifier(scytheMaster(attacker, user));
    }

    private DamageModifier scytheMaster(ServerPlayerEntity player, User user) {
        var ability = Abilities.SCYTHE_MASTER;
        if (isDisabled(ability) || failsChecks(player, ability)) {
            return DamageModifier.none();
        }
        if (user.getAbilityLevel(ability) <= 0) {
            return DamageModifier.none();
        }
        return new DamageModifier(getValue(ability, user) / 100.0, DamageModifier.Operation.ADD_COMBINED);
    }
}
