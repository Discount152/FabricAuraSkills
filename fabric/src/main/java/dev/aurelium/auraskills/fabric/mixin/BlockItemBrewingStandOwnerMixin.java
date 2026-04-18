package dev.aurelium.auraskills.fabric.mixin;

import dev.aurelium.auraskills.fabric.FabricBrewingOwnerTracker;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Bukkit {@code BrewingLeveler#onBrewingStandPlace}: record placer as stand owner when a brewing stand is placed.
 */
@Mixin(BlockItem.class)
public abstract class BlockItemBrewingStandOwnerMixin {

    @Inject(method = "place", at = @At("RETURN"))
    private void auraskills$brewingStandPlaced(ItemPlacementContext context, CallbackInfoReturnable<ActionResult> cir) {
        ActionResult result = cir.getReturnValue();
        if (!result.isAccepted()) {
            return;
        }
        BlockItem self = (BlockItem) (Object) this;
        if (!self.getBlock().equals(Blocks.BREWING_STAND)) {
            return;
        }
        if (!(context.getWorld() instanceof ServerWorld sw)) {
            return;
        }
        if (!(context.getPlayer() instanceof ServerPlayerEntity sp)) {
            return;
        }
        FabricBrewingOwnerTracker.setOwner(sw, context.getBlockPos(), sp.getUuid());
    }
}
