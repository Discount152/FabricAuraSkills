package dev.aurelium.auraskills.fabric.mixin;

import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Bukkit {@code RegionBlockListener#checkPlace} — when a block is successfully placed, optionally record it for
 * {@code check_replace} (see {@code FabricRegionManager#handleBlockPlace}).
 */
@Mixin(BlockItem.class)
public abstract class BlockItemPlacedBlockMixin {

    @Inject(method = "place", at = @At("RETURN"))
    private void auraskills$onPlaced(ItemPlacementContext context, CallbackInfoReturnable<ActionResult> cir) {
        ActionResult result = cir.getReturnValue();
        if (!result.isAccepted()) {
            return;
        }
        if (!(context.getWorld() instanceof ServerWorld world)) {
            return;
        }
        FabricAuraSkills plugin = FabricAuraSkills.getInstance();
        if (plugin == null || !plugin.isRuntimeReady()) {
            return;
        }
        BlockState newState = world.getBlockState(context.getBlockPos());
        if (newState.isAir()) {
            return;
        }
        plugin.getFabricRegionManager().handleBlockPlace(world, context.getBlockPos(), newState);
    }
}
