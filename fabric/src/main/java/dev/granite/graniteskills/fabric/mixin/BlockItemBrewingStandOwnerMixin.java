package dev.granite.graniteskills.fabric.mixin;

import dev.granite.graniteskills.fabric.FabricBrewingOwnerTracker;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Blocks;
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
    private void graniteskills$brewingStandPlaced(BlockPlaceContext context, CallbackInfoReturnable<InteractionResult> cir) {
        InteractionResult result = cir.getReturnValue();
        if (!result.consumesAction()) {
            return;
        }
        BlockItem self = (BlockItem) (Object) this;
        if (!self.getBlock().equals(Blocks.BREWING_STAND)) {
            return;
        }
        if (!(context.getLevel() instanceof ServerLevel sw)) {
            return;
        }
        if (!(context.getPlayer() instanceof ServerPlayer sp)) {
            return;
        }
        FabricBrewingOwnerTracker.setOwner(sw, context.getClickedPos(), sp.getUUID());
    }
}
