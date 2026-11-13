package dev.granite.graniteskills.fabric.mixin;

import dev.granite.graniteskills.fabric.FabricBrewingOwnerTracker;
import dev.granite.graniteskills.fabric.FabricBrewingTakeoutTracker;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Loom cannot apply inherited {@code markRemoved} on {@link BrewingStandBlockEntity} only; inject on {@link BlockEntity}.
 */
@Mixin(BlockEntity.class)
public abstract class BlockEntityBrewingCleanupMixin {

    @Inject(method = "setRemoved", at = @At("HEAD"))
    private void graniteskills$clearBrewingTakeoutTracker(CallbackInfo ci) {
        BlockEntity self = (BlockEntity) (Object) this;
        if (!(self instanceof BrewingStandBlockEntity)) {
            return;
        }
        Level world = self.getLevel();
        if (world instanceof ServerLevel sw) {
            FabricBrewingTakeoutTracker.remove(sw, self.getBlockPos());
            FabricBrewingOwnerTracker.remove(sw, self.getBlockPos());
        }
    }
}
