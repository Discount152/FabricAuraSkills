package dev.granite.graniteskills.fabric.mixin;

import dev.granite.graniteskills.fabric.requirement.FabricHarvestRequirement;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Harvest requirement for ripe sweet berry bushes (Paper {@code PlayerHarvestBlockEvent}).
 */
@Mixin(SweetBerryBushBlock.class)
public abstract class SweetBerryBushRequirementMixin {

    @Inject(method = "useWithoutItem", at = @At("HEAD"), cancellable = true)
    private void graniteskills$harvestRequirementOnUse(BlockState state, Level world, net.minecraft.core.BlockPos pos,
            Player player, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        if (world.isClientSide()) {
            return;
        }
        if (state.getValue(SweetBerryBushBlock.AGE) < SweetBerryBushBlock.MAX_AGE) {
            return;
        }
        if (!FabricHarvestRequirement.allowHarvest(player, state)) {
            cir.setReturnValue(InteractionResult.FAIL);
        }
    }
}
