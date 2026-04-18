package dev.aurelium.auraskills.fabric.mixin;

import dev.aurelium.auraskills.fabric.requirement.FabricHarvestRequirement;
import net.minecraft.block.BeehiveBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Harvest requirement for honey/shears interactions on bee nests and hives.
 */
@Mixin(BeehiveBlock.class)
public abstract class BeehiveHarvestRequirementMixin {

    @Inject(method = "onUseWithItem", at = @At("HEAD"), cancellable = true)
    private void auraskills$harvestRequirementHoney(ItemStack stack, BlockState state, World world,
            net.minecraft.util.math.BlockPos pos, PlayerEntity player, net.minecraft.util.Hand hand, BlockHitResult hit,
            CallbackInfoReturnable<ActionResult> cir) {
        if (world.isClient()) {
            return;
        }
        int honey = state.get(BeehiveBlock.HONEY_LEVEL);
        if (honey <= 0) {
            return;
        }
        if (!stack.isOf(Items.SHEARS) && !stack.isOf(Items.GLASS_BOTTLE)) {
            return;
        }
        if (!FabricHarvestRequirement.allowHarvest(player, state)) {
            cir.setReturnValue(ActionResult.FAIL);
        }
    }
}
