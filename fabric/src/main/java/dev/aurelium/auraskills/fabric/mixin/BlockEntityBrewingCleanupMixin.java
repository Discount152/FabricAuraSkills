package dev.aurelium.auraskills.fabric.mixin;

import dev.aurelium.auraskills.fabric.FabricBrewingOwnerTracker;
import dev.aurelium.auraskills.fabric.FabricBrewingTakeoutTracker;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Loom cannot apply inherited {@code markRemoved} on {@link BrewingStandBlockEntity} only; inject on {@link BlockEntity}.
 */
@Mixin(BlockEntity.class)
public abstract class BlockEntityBrewingCleanupMixin {

    @Inject(method = "markRemoved", at = @At("HEAD"))
    private void auraskills$clearBrewingTakeoutTracker(CallbackInfo ci) {
        BlockEntity self = (BlockEntity) (Object) this;
        if (!(self instanceof BrewingStandBlockEntity)) {
            return;
        }
        World world = self.getWorld();
        if (world instanceof ServerWorld sw) {
            FabricBrewingTakeoutTracker.remove(sw, self.getPos());
            FabricBrewingOwnerTracker.remove(sw, self.getPos());
        }
    }
}
