package dev.granite.graniteskills.fabric.mixin;

import dev.granite.graniteskills.fabric.FabricGrindstoneXpHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Grindstone does not declare {@code onSlotClick} in its class file (inherited from {@link AbstractContainerMenu}),
 * so Loom cannot target it on {@link GrindstoneMenu} directly. Filter here.
 */
@Mixin(AbstractContainerMenu.class)
public abstract class ScreenHandlerGrindstoneXpMixin {

    @Unique
    private boolean graniteskillsGrindstoneTrackingOutputClick;

    @Unique
    private ItemStack graniteskillsOutputBeforeClick = ItemStack.EMPTY;

    @Unique
    private ItemStack graniteskillsInput1Before = ItemStack.EMPTY;

    @Unique
    private ItemStack graniteskillsInput2Before = ItemStack.EMPTY;

    @Inject(method = "clicked", at = @At("HEAD"))
    private void graniteskills$grindstoneCapture(int slotIndex, int button, ContainerInput actionType, Player player, CallbackInfo ci) {
        if (!((Object) this instanceof GrindstoneMenu self)) {
            return;
        }
        graniteskillsGrindstoneTrackingOutputClick = false;
        if (slotIndex != GrindstoneMenu.RESULT_SLOT) {
            return;
        }
        graniteskillsGrindstoneTrackingOutputClick = true;
        graniteskillsOutputBeforeClick = self.getSlot(GrindstoneMenu.RESULT_SLOT).getItem().copy();
        graniteskillsInput1Before = self.getSlot(GrindstoneMenu.INPUT_SLOT).getItem().copy();
        graniteskillsInput2Before = self.getSlot(GrindstoneMenu.ADDITIONAL_SLOT).getItem().copy();
    }

    @Inject(method = "clicked", at = @At("RETURN"))
    private void graniteskills$grindstoneAward(int slotIndex, int button, ContainerInput actionType, Player player, CallbackInfo ci) {
        if (!((Object) this instanceof GrindstoneMenu self)) {
            return;
        }
        try {
            if (!graniteskillsGrindstoneTrackingOutputClick) {
                return;
            }
            if (!(player instanceof ServerPlayer sp) || player.level().isClientSide()) {
                return;
            }
            if (slotIndex != GrindstoneMenu.RESULT_SLOT) {
                return;
            }
            ItemStack after = self.getSlot(GrindstoneMenu.RESULT_SLOT).getItem();
            if (graniteskillsOutputBeforeClick.isEmpty()) {
                return;
            }
            if (!ItemStack.isSameItemSameComponents(graniteskillsOutputBeforeClick, after)
                    || after.getCount() < graniteskillsOutputBeforeClick.getCount()) {
                FabricGrindstoneXpHandler.onGrindstoneTake(sp, self, graniteskillsInput1Before, graniteskillsInput2Before);
            }
        } finally {
            graniteskillsGrindstoneTrackingOutputClick = false;
            graniteskillsOutputBeforeClick = ItemStack.EMPTY;
            graniteskillsInput1Before = ItemStack.EMPTY;
            graniteskillsInput2Before = ItemStack.EMPTY;
        }
    }
}
