package dev.granite.graniteskills.fabric.mixin;

import dev.granite.graniteskills.fabric.FabricSpawnerMobTracker;
import dev.granite.graniteskills.fabric.FabricSpawnerSpawnContext;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerLevel.class)
public abstract class ServerWorldSpawnerMixin {

    @Inject(method = "addFreshEntity", at = @At("RETURN"))
    private void graniteskills$markSpawnerMob(Entity entity, CallbackInfoReturnable<Boolean> cir) {
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
