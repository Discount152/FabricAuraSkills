package dev.aurelium.auraskills.fabric.mixin;

import dev.aurelium.auraskills.fabric.requirement.FabricHarvestRequirement;
import net.minecraft.block.BlockState;
import net.minecraft.block.SweetBerryBushBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Harvest requirement for ripe sweet berry bushes (Paper {@code PlayerHarvestBlockEvent}).
 */
@Mixin(SweetBerryBushBlock.class)
public abstract class SweetBerryBushRequirementMixin {

    @Inject(method = "onUse", at = @At("HEAD"), cancellable = true)
    private void auraskills$harvestRequirementOnUse(BlockState state, World world, net.minecraft.util.math.BlockPos pos,
            PlayerEntity player, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        if (world.isClient()) {
            return;
        }
        if (state.get(SweetBerryBushBlock.AGE) < SweetBerryBushBlock.MAX_AGE) {
            return;
        }
        if (!FabricHarvestRequirement.allowHarvest(player, state)) {
            cir.setReturnValue(ActionResult.FAIL);
        }
    }
}
