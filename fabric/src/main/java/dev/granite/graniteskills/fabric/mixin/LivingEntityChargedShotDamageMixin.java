package dev.granite.graniteskills.fabric.mixin;

import dev.granite.graniteskills.fabric.FabricChargedShot;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = LivingEntity.class, priority = 1100)
public abstract class LivingEntityChargedShotDamageMixin {

    @ModifyVariable(
            method = "hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            at = @At("HEAD"),
            ordinal = 2,
            argsOnly = true
    )
    private float graniteskills$chargedShotMultiplyProjectileDamage(float amount, ServerLevel world, DamageSource source) {
        return FabricChargedShot.modifyDamageFromChargedProjectile(world, source, amount);
    }
}
