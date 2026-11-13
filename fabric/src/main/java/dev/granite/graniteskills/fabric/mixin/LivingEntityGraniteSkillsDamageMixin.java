package dev.granite.graniteskills.fabric.mixin;

import dev.granite.graniteskills.fabric.damage.FabricDamagePipeline;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Applies GraniteSkills attack/defense damage modifiers after {@link LivingEntityChargedShotDamageMixin} (priority 1100).
 */
@Mixin(value = LivingEntity.class, priority = 900)
public abstract class LivingEntityGraniteSkillsDamageMixin {

    @ModifyVariable(
            method = "hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            at = @At("HEAD"),
            ordinal = 2,
            argsOnly = true
    )
    private float graniteskills$traitDamage(float amount, ServerLevel world, DamageSource source) {
        LivingEntity self = (LivingEntity) (Object) this;
        return FabricDamagePipeline.modifyIncomingDamage(world, self, source, amount);
    }
}
