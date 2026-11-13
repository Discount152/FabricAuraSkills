package dev.aurelium.auraskills.fabric.mixin;

import dev.aurelium.auraskills.fabric.FabricSpawnerSpawnContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BaseSpawner.class)
public abstract class MobSpawnerLogicMixin {

    @Inject(method = "delay", at = @At("HEAD"))
    private void auraskills$spawnerContextEnter(Level world, BlockPos pos, CallbackInfo ci) {
        FabricSpawnerSpawnContext.enter();
    }

    @Inject(method = "delay", at = @At("TAIL"))
    private void auraskills$spawnerContextExit(Level world, BlockPos pos, CallbackInfo ci) {
        FabricSpawnerSpawnContext.exit();
    }
}
