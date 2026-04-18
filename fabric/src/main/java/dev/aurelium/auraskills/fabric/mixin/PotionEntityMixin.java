package dev.aurelium.auraskills.fabric.mixin;

import dev.aurelium.auraskills.fabric.FabricPotionSplashHandler;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PotionEntity.class)
public abstract class PotionEntityMixin {

    @Inject(method = "onCollision", at = @At("HEAD"))
    private void auraskills$splashXp(HitResult hitResult, CallbackInfo ci) {
        FabricPotionSplashHandler.onPotionCollision((PotionEntity) (Object) this);
    }
}
