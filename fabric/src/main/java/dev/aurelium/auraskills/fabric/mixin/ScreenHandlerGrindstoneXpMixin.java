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
    private boolean auraskills$grindstoneTrackingOutputClick;

    @Unique
    private ItemStack auraskills$outputBeforeClick = ItemStack.EMPTY;

    @Unique
    private ItemStack auraskills$input1Before = ItemStack.EMPTY;

    @Unique
    private ItemStack auraskills$input2Before = ItemStack.EMPTY;

    @Inject(method = "clicked", at = @At("HEAD"))
    private void auraskills$grindstoneCapture(int slotIndex, int button, ContainerInput actionType, Player player, CallbackInfo ci) {
        if (!((Object) this instanceof GrindstoneMenu self)) {
            return;
        }
        auraskills$grindstoneTrackingOutputClick = false;
        if (slotIndex != GrindstoneMenu.RESULT_SLOT) {
            return;
        }
        auraskills$grindstoneTrackingOutputClick = true;
        auraskills$outputBeforeClick = self.getSlot(GrindstoneMenu.RESULT_SLOT).getItem().copy();
        auraskills$input1Before = self.getSlot(GrindstoneMenu.INPUT_SLOT).getItem().copy();
        auraskills$input2Before = self.getSlot(GrindstoneMenu.ADDITIONAL_SLOT).getItem().copy();
    }

    @Inject(method = "clicked", at = @At("RETURN"))
    private void auraskills$grindstoneAward(int slotIndex, int button, ContainerInput actionType, Player player, CallbackInfo ci) {
        if (!((Object) this instanceof GrindstoneMenu self)) {
            return;
        }
        try {
            if (!auraskills$grindstoneTrackingOutputClick) {
                return;
            }
            if (!(player instanceof ServerPlayer sp) || player.level().isClientSide()) {
                return;
            }
            if (slotIndex != GrindstoneMenu.RESULT_SLOT) {
                return;
            }
            ItemStack after = self.getSlot(GrindstoneMenu.RESULT_SLOT).getItem();
            if (auraskills$outputBeforeClick.isEmpty()) {
                return;
            }
            if (!ItemStack.isSameItemSameComponents(auraskills$outputBeforeClick, after)
                    || after.getCount() < auraskills$outputBeforeClick.getCount()) {
                FabricGrindstoneXpHandler.onGrindstoneTake(sp, self, auraskills$input1Before, auraskills$input2Before);
            }
        } finally {
            auraskills$grindstoneTrackingOutputClick = false;
            auraskills$outputBeforeClick = ItemStack.EMPTY;
            auraskills$input1Before = ItemStack.EMPTY;
            auraskills$input2Before = ItemStack.EMPTY;
        }
    }
}
