package dev.aurelium.auraskills.fabric.mixin;

import dev.aurelium.auraskills.fabric.FabricBrewingTakeoutTracker;
import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * {@link BrewingStandBlockEntity} inherits {@code setStack} from {@link LockableContainerBlockEntity}; Loom only
 * remaps injections declared on the declaring class.
 */
@Mixin(LockableContainerBlockEntity.class)
public abstract class LockableContainerBrewingStandSetStackMixin {

    @Inject(method = "setStack", at = @At("TAIL"))
    private void auraskills$clearTakeoutWhenBrewingOutputEmptied(int slot, ItemStack stack, CallbackInfo ci) {
        if (!((Object) this instanceof BrewingStandBlockEntity be)) {
            return;
        }
        if (slot < 0 || slot > 2) {
            return;
        }
        if (!stack.isEmpty()) {
            return;
        }
        if (be.getWorld() == null
                || be.getWorld().isClient()
                || !(be.getWorld() instanceof ServerWorld sw)) {
            return;
        }
        FabricBrewingTakeoutTracker.clearTakeoutStateIfOutputEmptied(sw, be.getPos(), slot);
    }
}
