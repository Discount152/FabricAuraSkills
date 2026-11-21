package dev.granite.graniteskills.fabric.mixin;

import dev.granite.graniteskills.fabric.damage.FabricDamagePipeline;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

/**
 * Applies GraniteSkills attack/defense modifiers only when vanilla is about to call
 * {@code actuallyHurt} — after invulnerability, death, fire-resist, and cooldown rejection.
 */
@Mixin(value = LivingEntity.class, priority = 900)
public abstract class LivingEntityGraniteSkillsDamageMixin {

    @ModifyArgs(
            method = "hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;actuallyHurt(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)V"
            )
    )
    private void graniteskills$traitDamage(Args args) {
        ServerLevel world = args.get(0);
        DamageSource source = args.get(1);
        float amount = args.get(2);
        LivingEntity self = (LivingEntity) (Object) this;
        args.set(2, FabricDamagePipeline.modifyIncomingDamage(world, self, source, amount));
    }
}
