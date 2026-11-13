package dev.granite.graniteskills.fabric.mixin;

import dev.granite.graniteskills.fabric.ability.FabricDefenseAbilities;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Ports Bukkit {@code DefenseAbilities#noDebuff} for splash and lingering harmful effects: harmful instances applied with
 * a {@link net.minecraft.world.entity.projectile.throwableitemprojectile.AbstractThrownPotion} or {@link net.minecraft.world.entity.AreaEffectCloud}
 * source can be blocked per roll.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityNoDebuffSplashMixin {

    @Inject(method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z",
            at = @At("HEAD"), cancellable = true)
    private void graniteskills$noDebuffSplash(MobEffectInstance effect, Entity source,
            CallbackInfoReturnable<Boolean> cir) {
        if (FabricDefenseAbilities.shouldBlockSplashDebuff((LivingEntity) (Object) this, effect, source)) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
}
