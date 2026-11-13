package dev.aurelium.auraskills.fabric.mixin;

import dev.aurelium.auraskills.fabric.ability.FabricFarmingAbilities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CropBlock.class)
public abstract class CropBlockGrowthMixin {

    @Inject(method = "growCrops", at = @At("RETURN"))
    private void auraskills$growthAura(Level level, BlockPos pos, BlockState state, CallbackInfo ci) {
        FabricFarmingAbilities.onCropGrown(level, pos);
    }
}
