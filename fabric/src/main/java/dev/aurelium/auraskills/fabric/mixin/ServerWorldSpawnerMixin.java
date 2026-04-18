package dev.aurelium.auraskills.fabric.mixin;

import dev.aurelium.auraskills.fabric.FabricSpawnerMobTracker;
import dev.aurelium.auraskills.fabric.FabricSpawnerSpawnContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerWorld.class)
public abstract class ServerWorldSpawnerMixin {

    @Inject(method = "spawnEntity", at = @At("RETURN"))
    private void auraskills$markSpawnerMob(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (Boolean.FALSE.equals(cir.getReturnValue())) {
            return;
        }
        if (!FabricSpawnerSpawnContext.active()) {
            return;
        }
        if (entity instanceof LivingEntity living) {
            FabricSpawnerMobTracker.tryMarkFromSpawner(living);
        }
    }
}
