package dev.aurelium.auraskills.fabric.mixin;

import dev.aurelium.auraskills.fabric.FabricPotionSplashHandler;
import net.minecraft.world.entity.projectile.throwableitemprojectile.AbstractThrownPotion;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractThrownPotion.class)
public abstract class PotionEntityMixin {

    @Inject(method = "onHit", at = @At("HEAD"))
    private void auraskills$splashXp(HitResult hitResult, CallbackInfo ci) {
        FabricPotionSplashHandler.onPotionCollision((AbstractThrownPotion) (Object) this);
    }
}
