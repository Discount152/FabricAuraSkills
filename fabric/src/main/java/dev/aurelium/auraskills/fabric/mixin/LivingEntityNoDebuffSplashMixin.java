package dev.aurelium.auraskills.fabric.mixin;

import dev.aurelium.auraskills.fabric.ability.FabricDefenseAbilities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Ports Bukkit {@code DefenseAbilities#noDebuff} for splash and lingering harmful effects: harmful instances applied with
 * a {@link net.minecraft.entity.projectile.thrown.PotionEntity} or {@link net.minecraft.entity.AreaEffectCloudEntity}
 * source can be blocked per roll.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityNoDebuffSplashMixin {

    @Inject(method = "addStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;Lnet/minecraft/entity/Entity;)Z",
            at = @At("HEAD"), cancellable = true)
    private void auraskills$noDebuffSplash(StatusEffectInstance effect, Entity source,
            CallbackInfoReturnable<Boolean> cir) {
        if (FabricDefenseAbilities.shouldBlockSplashDebuff((LivingEntity) (Object) this, effect, source)) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
}
