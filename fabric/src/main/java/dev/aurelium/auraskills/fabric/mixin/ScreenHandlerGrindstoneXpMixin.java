package dev.aurelium.auraskills.fabric.mixin;

import dev.aurelium.auraskills.fabric.FabricGrindstoneXpHandler;
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
    private boolean auraskillsGrindstoneTrackingOutputClick;

    @Unique
    private ItemStack auraskillsOutputBeforeClick = ItemStack.EMPTY;

    @Unique
    private ItemStack auraskillsInput1Before = ItemStack.EMPTY;

    @Unique
    private ItemStack auraskillsInput2Before = ItemStack.EMPTY;

    @Inject(method = "clicked", at = @At("HEAD"))
    private void auraskills$grindstoneCapture(int slotIndex, int button, ContainerInput actionType, Player player, CallbackInfo ci) {
        if (!((Object) this instanceof GrindstoneMenu self)) {
            return;
        }
        auraskillsGrindstoneTrackingOutputClick = false;
        if (slotIndex != GrindstoneMenu.RESULT_SLOT) {
            return;
        }
        auraskillsGrindstoneTrackingOutputClick = true;
        auraskillsOutputBeforeClick = self.getSlot(GrindstoneMenu.RESULT_SLOT).getItem().copy();
        auraskillsInput1Before = self.getSlot(GrindstoneMenu.INPUT_SLOT).getItem().copy();
        auraskillsInput2Before = self.getSlot(GrindstoneMenu.ADDITIONAL_SLOT).getItem().copy();
    }

    @Inject(method = "clicked", at = @At("RETURN"))
    private void auraskills$grindstoneAward(int slotIndex, int button, ContainerInput actionType, Player player, CallbackInfo ci) {
        if (!((Object) this instanceof GrindstoneMenu self)) {
            return;
        }
        try {
            if (!auraskillsGrindstoneTrackingOutputClick) {
                return;
            }
            if (!(player instanceof ServerPlayer sp) || player.level().isClientSide()) {
                return;
            }
            if (slotIndex != GrindstoneMenu.RESULT_SLOT) {
                return;
            }
            ItemStack after = self.getSlot(GrindstoneMenu.RESULT_SLOT).getItem();
            if (auraskillsOutputBeforeClick.isEmpty()) {
                return;
            }
            if (!ItemStack.isSameItemSameComponents(auraskillsOutputBeforeClick, after)
                    || after.getCount() < auraskillsOutputBeforeClick.getCount()) {
                FabricGrindstoneXpHandler.onGrindstoneTake(sp, self, auraskillsInput1Before, auraskillsInput2Before);
            }
        } finally {
            auraskillsGrindstoneTrackingOutputClick = false;
            auraskillsOutputBeforeClick = ItemStack.EMPTY;
            auraskillsInput1Before = ItemStack.EMPTY;
            auraskillsInput2Before = ItemStack.EMPTY;
        }
    }
}
