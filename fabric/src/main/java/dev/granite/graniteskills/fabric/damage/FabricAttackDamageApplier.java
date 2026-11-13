package dev.granite.graniteskills.fabric.damage;

import dev.granite.graniteskills.api.damage.DamageModifier;
import dev.granite.graniteskills.api.damage.DamageType;
import dev.granite.graniteskills.api.trait.Trait;
import dev.granite.graniteskills.api.trait.Traits;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import net.minecraft.server.level.ServerPlayer;

public final class FabricAttackDamageApplier {

    private FabricAttackDamageApplier() {
    }

    public static void apply(FabricGraniteSkills plugin, FabricDamageMeta meta, ServerPlayer attacker) {
        Trait trait = Traits.ATTACK_DAMAGE;
        if (!trait.isEnabled()) {
            return;
        }
        if (meta.getDamageType() == DamageType.HAND && !trait.optionBoolean("hand_damage")) {
            return;
        }
        if (meta.getDamageType() == DamageType.BOW && !trait.optionBoolean("bow_damage")) {
            return;
        }
        User user = plugin.getUserManager().getUser(attacker.getUUID());
        double value = user.getBonusTraitLevel(Traits.ATTACK_DAMAGE);
        if (Traits.ATTACK_DAMAGE.optionBoolean("use_percent")) {
            meta.addAttackModifier(new DamageModifier(value / 100.0, DamageModifier.Operation.MULTIPLY));
        } else {
            meta.addAttackModifier(new DamageModifier(value, DamageModifier.Operation.ADD_BASE));
        }
    }
}
