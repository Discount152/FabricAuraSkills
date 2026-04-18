package dev.aurelium.auraskills.fabric.mixin;

import dev.aurelium.auraskills.fabric.FabricSpawnerSpawnContext;
import net.minecraft.block.spawner.MobSpawnerLogic;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobSpawnerLogic.class)
public abstract class MobSpawnerLogicMixin {

    @Inject(method = "updateSpawns", at = @At("HEAD"))
    private void auraskills$spawnerContextEnter(World world, BlockPos pos, CallbackInfo ci) {
        FabricSpawnerSpawnContext.enter();
    }

    @Inject(method = "updateSpawns", at = @At("TAIL"))
    private void auraskills$spawnerContextExit(World world, BlockPos pos, CallbackInfo ci) {
        FabricSpawnerSpawnContext.exit();
    }
}
