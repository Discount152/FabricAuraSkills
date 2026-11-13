package dev.granite.graniteskills.fabric.mixin;

import dev.granite.graniteskills.fabric.ability.FabricFarmingAbilities;
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
    private void graniteskills$growthAura(Level level, BlockPos pos, BlockState state, CallbackInfo ci) {
        FabricFarmingAbilities.onCropGrown(level, pos);
    }
}
