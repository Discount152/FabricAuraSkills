package dev.aurelium.auraskills.fabric.damage;

import dev.aurelium.auraskills.api.damage.DamageModifier;
import dev.aurelium.auraskills.api.damage.DamageType;
import dev.aurelium.auraskills.api.trait.Trait;
import dev.aurelium.auraskills.api.trait.Traits;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import net.minecraft.server.level.ServerPlayer;

public final class FabricAttackDamageApplier {

    private FabricAttackDamageApplier() {
    }

    public static void apply(FabricAuraSkills plugin, FabricDamageMeta meta, ServerPlayer attacker) {
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
