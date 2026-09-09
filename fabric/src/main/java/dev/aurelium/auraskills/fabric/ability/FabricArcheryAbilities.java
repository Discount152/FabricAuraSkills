package dev.aurelium.auraskills.fabric.ability;

import dev.aurelium.auraskills.api.ability.Abilities;
import dev.aurelium.auraskills.api.ability.Ability;
import dev.aurelium.auraskills.api.damage.DamageModifier;
import dev.aurelium.auraskills.api.damage.DamageType;
import dev.aurelium.auraskills.api.util.NumberUtil;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.common.util.text.TextUtil;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.damage.FabricDamageMeta;
import net.minecraft.server.network.ServerPlayerEntity;

public final class FabricArcheryAbilities extends FabricAbilityImpl implements FabricDamagePipelineContributor {

    public FabricArcheryAbilities(FabricAuraSkills plugin) {
        super(plugin, Abilities.RETRIEVAL, Abilities.ARCHER, Abilities.BOW_MASTER, Abilities.PIERCING, Abilities.STUN);
    }

    @Override
    public String replaceDescPlaceholders(String input, Ability ability, User user) {
        if (ability.equals(Abilities.RETRIEVAL)) {
            return TextUtil.replace(input, "{time}", NumberUtil.format1(ability.optionDouble("delay_sec", 3)));
        }
        if (ability.equals(Abilities.STUN)) {
            return TextUtil.replace(
                    input,
                    "{reduction_percent}",
                    NumberUtil.format1(ability.optionDouble("speed_reduction", 0.2) * 100.0));
        }
        return input;
    }

    @Override
    public void contributeAttackDamage(FabricAuraSkills plugin, FabricDamageMeta meta, ServerPlayerEntity attacker) {
        if (meta.getDamageType() != DamageType.BOW) {
            return;
        }
        User user = plugin.getUserManager().getUser(attacker.getUuid());
        meta.addAttackModifier(bowMaster(attacker, user));
    }

    private DamageModifier bowMaster(ServerPlayerEntity player, User user) {
        var ability = Abilities.BOW_MASTER;
        if (isDisabled(ability) || failsChecks(player, ability)) {
            return DamageModifier.none();
        }
        if (user.getAbilityLevel(ability) <= 0) {
            return DamageModifier.none();
        }
        return new DamageModifier(getValue(ability, user) / 100.0, DamageModifier.Operation.ADD_COMBINED);
    }
}
