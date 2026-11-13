package dev.granite.graniteskills.fabric.mixin;

import dev.granite.graniteskills.fabric.requirement.FabricHarvestRequirement;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Harvest requirement for honey/shears interactions on bee nests and hives.
 */
@Mixin(BeehiveBlock.class)
public abstract class BeehiveHarvestRequirementMixin {

    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    private void graniteskills$harvestRequirementHoney(ItemStack stack, BlockState state, Level world,
            net.minecraft.core.BlockPos pos, Player player, net.minecraft.world.InteractionHand hand, BlockHitResult hit,
            CallbackInfoReturnable<InteractionResult> cir) {
        if (world.isClientSide()) {
            return;
        }
        int honey = state.getValue(BeehiveBlock.HONEY_LEVEL);
        if (honey <= 0) {
            return;
        }
        if (!stack.is(Items.SHEARS) && !stack.is(Items.GLASS_BOTTLE)) {
            return;
        }
        if (!FabricHarvestRequirement.allowHarvest(player, state)) {
            cir.setReturnValue(InteractionResult.FAIL);
        }
    }
}
