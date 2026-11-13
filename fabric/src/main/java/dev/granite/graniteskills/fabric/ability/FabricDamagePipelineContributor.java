package dev.granite.graniteskills.fabric.ability;

import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import dev.granite.graniteskills.fabric.damage.FabricDamageMeta;
import net.minecraft.server.level.ServerPlayer;

/**
 * Passive abilities that adjust outgoing or incoming damage via {@link FabricDamageMeta}.
 */
public interface FabricDamagePipelineContributor {

    default void contributeAttackDamage(FabricGraniteSkills plugin, FabricDamageMeta meta, ServerPlayer attacker) {
    }

    default void contributeDefenseDamage(FabricGraniteSkills plugin, FabricDamageMeta meta, ServerPlayer victim) {
    }
}
