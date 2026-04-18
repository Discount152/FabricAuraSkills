package dev.aurelium.auraskills.fabric.mixin;

import dev.aurelium.auraskills.fabric.damage.FabricDamagePipeline;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Applies AuraSkills attack/defense damage modifiers after {@link LivingEntityChargedShotDamageMixin} (priority 1100).
 */
@Mixin(value = LivingEntity.class, priority = 900)
public abstract class LivingEntityAuraSkillsDamageMixin {

    @ModifyVariable(
            method = "damage(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/damage/DamageSource;F)Z",
            at = @At("HEAD"),
            ordinal = 2,
            argsOnly = true
    )
    private float auraskills$traitDamage(float amount, ServerWorld world, DamageSource source) {
        LivingEntity self = (LivingEntity) (Object) this;
        return FabricDamagePipeline.modifyIncomingDamage(world, self, source, amount);
    }
}
