package dev.aurelium.auraskills.fabric.ability;

import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.damage.FabricDamageMeta;
import net.minecraft.server.level.ServerPlayer;

/**
 * Passive abilities that adjust outgoing or incoming damage via {@link FabricDamageMeta}.
 */
public interface FabricDamagePipelineContributor {

    default void contributeAttackDamage(FabricAuraSkills plugin, FabricDamageMeta meta, ServerPlayer attacker) {
    }

    default void contributeDefenseDamage(FabricAuraSkills plugin, FabricDamageMeta meta, ServerPlayer victim) {
    }
}
