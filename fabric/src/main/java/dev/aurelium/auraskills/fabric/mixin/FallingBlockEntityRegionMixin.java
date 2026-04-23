package dev.aurelium.auraskills.fabric.mixin;

import dev.aurelium.auraskills.fabric.region.FabricRegionBlockIntegrity;
import net.minecraft.block.BlockState;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FallingBlockEntity.class)
public class FallingBlockEntityRegionMixin {

    @Inject(method = "spawnFromBlock", at = @At("RETURN"))
    private static void auraskills$afterSpawnFromBlock(
            World world, BlockPos pos, BlockState state, CallbackInfoReturnable<FallingBlockEntity> cir) {
        FallingBlockEntity entity = cir.getReturnValue();
        if (entity == null || world.isClient()) {
            return;
        }
        FabricRegionBlockIntegrity.onFallingBlockSpawned(world, pos, state, entity);
    }
}
