package dev.aurelium.auraskills.fabric.mixin;

import dev.aurelium.auraskills.fabric.region.FabricRegionBlockIntegrity;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ModifiableWorld;
import net.minecraft.world.gen.feature.TreeFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TreeFeature.class)
public class TreeFeatureSetBlockMixin {

    @Inject(method = "setBlockState", at = @At("HEAD"))
    private void auraskills$onTreeSetBlock(ModifiableWorld world, BlockPos pos, BlockState state, CallbackInfo ci) {
        if (world instanceof ServerWorld sw) {
            FabricRegionBlockIntegrity.onTreePlaceBlock(sw, pos);
        }
    }
}
