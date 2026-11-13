package dev.granite.graniteskills.fabric.damage;

import dev.granite.graniteskills.api.damage.DamageModifier;
import dev.granite.graniteskills.api.damage.DamageType;
import dev.granite.graniteskills.api.trait.Traits;
import dev.granite.graniteskills.common.config.Option;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import dev.granite.graniteskills.fabric.FabricTraitManager;
import dev.granite.graniteskills.fabric.trait.FabricCritChanceTrait;
import net.minecraft.server.level.ServerPlayer;

public final class FabricCriticalHandler {

    private FabricCriticalHandler() {
    }

    public static void apply(FabricGraniteSkills plugin, FabricDamageMeta meta, ServerPlayer attacker) {
        DamageType dt = meta.getDamageType();
        if (!criticalEnabled(plugin, dt)) {
            return;
        }
        User user = plugin.getUserManager().getUser(attacker.getUUID());
        FabricCritChanceTrait critTrait = ((FabricTraitManager) plugin.getTraitManager()).getTraitImpl(FabricCritChanceTrait.class);
        if (!critTrait.isCrit(user)) {
            return;
        }
        double value = user.getEffectiveTraitLevel(Traits.CRIT_DAMAGE) / 100.0;
        meta.addAttackModifier(new DamageModifier(value, DamageModifier.Operation.ADD_COMBINED));
    }

    private static boolean criticalEnabled(FabricGraniteSkills plugin, DamageType damageType) {
        Option opt = switch (damageType) {
            case SWORD -> Option.CRITICAL_ENABLED_SWORD;
            case BOW -> Option.CRITICAL_ENABLED_BOW;
            case AXE -> Option.CRITICAL_ENABLED_AXE;
            case PICKAXE -> Option.CRITICAL_ENABLED_PICKAXE;
            case SHOVEL -> Option.CRITICAL_ENABLED_SHOVEL;
            case HOE -> Option.CRITICAL_ENABLED_HOE;
            case HAND -> Option.CRITICAL_ENABLED_HAND;
            case OTHER -> Option.CRITICAL_ENABLED_OTHER;
        };
        return plugin.configBoolean(opt);
    }
}
