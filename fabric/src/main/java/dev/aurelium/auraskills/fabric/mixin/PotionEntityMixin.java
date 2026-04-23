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

    /**
     * Bukkit {@code PotionSplashLeveler} listens to both {@code PotionSplashEvent} and {@code LingeringPotionSplashEvent}
     * (same {@code ThrownPotion} entity on hit). In vanilla, splash and <strong>lingering</strong> potions are both
     * {@link net.minecraft.entity.projectile.thrown.PotionEntity} until collision; this hook runs once at collision.
     * See {@link FabricPotionSplashHandler#onPotionCollision}.
     */
    @Inject(method = "onCollision", at = @At("HEAD"))
    private void auraskills$splashXp(HitResult hitResult, CallbackInfo ci) {
        FabricPotionSplashHandler.onPotionCollision((PotionEntity) (Object) this);
    }
}
