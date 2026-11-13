package dev.aurelium.auraskills.fabric.mixin;

import dev.aurelium.auraskills.fabric.requirement.FabricHarvestRequirement;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CaveVines;
import net.minecraft.world.level.block.CaveVinesBlock;
import net.minecraft.world.level.block.CaveVinesPlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Harvest requirement for glow berries on cave vines (head and body).
 */
@Mixin({CaveVinesBlock.class, CaveVinesPlantBlock.class})
public abstract class CaveVinesHarvestRequirementMixin {

    @Inject(method = "useWithoutItem", at = @At("HEAD"), cancellable = true)
    private void auraskills$harvestRequirementGlowBerries(BlockState state, Level world, net.minecraft.core.BlockPos pos,
            Player player, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        if (world.isClientSide()) {
            return;
        }
        if (!state.getValue(CaveVines.BERRIES)) {
            return;
        }
        if (!FabricHarvestRequirement.allowHarvest(player, state)) {
            cir.setReturnValue(InteractionResult.FAIL);
        }
    }
}
