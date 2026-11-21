package dev.granite.graniteskills.fabric.mixin;

import dev.granite.graniteskills.fabric.ability.FabricFarmingAbilities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CropBlock.class)
public abstract class CropBlockGrowthMixin {

    @Unique
    private int graniteskills$ageBeforeGrowth;

    @Inject(method = "growCrops", at = @At("RETURN"))
    private void graniteskills$growthAura(Level level, BlockPos pos, BlockState state, CallbackInfo ci) {
        FabricFarmingAbilities.onCropGrown(level, pos);
    }

    @Inject(method = "randomTick", at = @At("HEAD"))
    private void graniteskills$captureAge(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        graniteskills$ageBeforeGrowth = ((CropBlock) (Object) this).getAge(state);
    }

    @Inject(method = "randomTick", at = @At("RETURN"))
    private void graniteskills$growthAuraRandomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        BlockState now = level.getBlockState(pos);
        if (!(now.getBlock() instanceof CropBlock crop) || crop.getAge(now) <= graniteskills$ageBeforeGrowth) {
            return;
        }
        FabricFarmingAbilities.onCropGrown(level, pos);
    }
}
