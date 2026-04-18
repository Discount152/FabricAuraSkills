package dev.aurelium.auraskills.fabric.mixin;

import dev.aurelium.auraskills.fabric.requirement.FabricHarvestRequirement;
import net.minecraft.block.BlockState;
import net.minecraft.block.CaveVines;
import net.minecraft.block.CaveVinesBodyBlock;
import net.minecraft.block.CaveVinesHeadBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Harvest requirement for glow berries on cave vines (head and body).
 */
@Mixin({CaveVinesHeadBlock.class, CaveVinesBodyBlock.class})
public abstract class CaveVinesHarvestRequirementMixin {

    @Inject(method = "onUse", at = @At("HEAD"), cancellable = true)
    private void auraskills$harvestRequirementGlowBerries(BlockState state, World world, net.minecraft.util.math.BlockPos pos,
            PlayerEntity player, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        if (world.isClient()) {
            return;
        }
        if (!state.get(CaveVines.BERRIES)) {
            return;
        }
        if (!FabricHarvestRequirement.allowHarvest(player, state)) {
            cir.setReturnValue(ActionResult.FAIL);
        }
    }
}
