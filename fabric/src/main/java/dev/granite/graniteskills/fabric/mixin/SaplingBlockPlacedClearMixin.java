package dev.granite.graniteskills.fabric.mixin;

import dev.granite.graniteskills.fabric.region.FabricPlacedBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * When a player-planted sapling grows, clear the placed mark on the sapling's Y so the base log
 * can award XP / Treecapitator (upstream StructureGrowEvent behaviour).
 */
@Mixin(SaplingBlock.class)
public abstract class SaplingBlockPlacedClearMixin {

    @Inject(method = "advanceTree", at = @At("HEAD"))
    private void graniteskills$clearPlacedSapling(
            ServerLevel level, BlockPos pos, BlockState state, RandomSource random, CallbackInfo ci) {
        FabricPlacedBlocks.clear(level, pos);
    }
}
