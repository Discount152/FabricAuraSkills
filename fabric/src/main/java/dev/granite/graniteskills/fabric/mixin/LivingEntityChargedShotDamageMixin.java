package dev.granite.graniteskills.fabric.mixin;

import dev.granite.graniteskills.fabric.FabricChargedShot;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

/**
 * Charged Shot multiplies only accepted hits (same {@code actuallyHurt} gate as the trait pipeline).
 */
@Mixin(value = LivingEntity.class, priority = 1100)
public abstract class LivingEntityChargedShotDamageMixin {

    @ModifyArgs(
            method = "hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;actuallyHurt(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)V"
            )
    )
    private void graniteskills$chargedShotMultiplyProjectileDamage(Args args) {
        ServerLevel world = args.get(0);
        DamageSource source = args.get(1);
        float amount = args.get(2);
        args.set(2, FabricChargedShot.modifyDamageFromChargedProjectile(world, source, amount));
    }
}
