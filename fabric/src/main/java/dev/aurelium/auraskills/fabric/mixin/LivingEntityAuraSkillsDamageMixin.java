package dev.aurelium.auraskills.fabric.mixin;

import dev.aurelium.auraskills.fabric.damage.FabricDamagePipeline;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Applies AuraSkills attack/defense damage modifiers after {@link LivingEntityChargedShotDamageMixin} (priority 1100).
 */
@Mixin(value = LivingEntity.class, priority = 900)
public abstract class LivingEntityAuraSkillsDamageMixin {

    @ModifyVariable(
            method = "hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            at = @At("HEAD"),
            ordinal = 2,
            argsOnly = true
    )
    private float auraskills$traitDamage(float amount, ServerLevel world, DamageSource source) {
        LivingEntity self = (LivingEntity) (Object) this;
        return FabricDamagePipeline.modifyIncomingDamage(world, self, source, amount);
    }
}
