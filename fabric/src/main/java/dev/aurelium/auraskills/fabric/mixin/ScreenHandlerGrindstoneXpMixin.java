package dev.aurelium.auraskills.fabric.mixin;

import dev.aurelium.auraskills.fabric.FabricGrindstoneXpHandler;
import dev.aurelium.auraskills.fabric.item.FabricItemInventoryUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GrindstoneScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Grindstone does not declare {@code onSlotClick} in its class file (inherited from {@link ScreenHandler}),
 * so Loom cannot target it on {@link GrindstoneScreenHandler} directly. Filter here.
 * <p>
 * Bukkit {@code GrindstoneLeveler} awards on result-slot take when {@code failsClickChecks} and {@code failsChecks} pass
 * (see {@code FabricGrindstoneXpHandler#allowsGrindstoneClickForXp}). This mixin: capture input/output copies at
 * {@code onSlotClick} start; on return, award only if the result actually moved (and Bukkit-style click rules pass) —
 * not on preview/idle clicks, non-takeout actions, or non-empty cursor.
 */
@Mixin(ScreenHandler.class)
public abstract class ScreenHandlerGrindstoneXpMixin {

    @Unique
    private boolean auraskills$grindstoneTrackingOutputClick;

    @Unique
    private ItemStack auraskills$outputBeforeClick = ItemStack.EMPTY;

    @Unique
    private ItemStack auraskills$input1Before = ItemStack.EMPTY;

    @Unique
    private ItemStack auraskills$input2Before = ItemStack.EMPTY;

    @Unique
    private boolean auraskills$grindstoneCursorEmptyAtStart;

    @Unique
    private boolean auraskills$grindstonePlayerStorageFull;

    @Inject(method = "onSlotClick", at = @At("HEAD"))
    private void auraskills$grindstoneCapture(int slotIndex, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        if (!((Object) this instanceof GrindstoneScreenHandler self)) {
            return;
        }
        auraskills$grindstoneTrackingOutputClick = false;
        if (slotIndex != GrindstoneScreenHandler.OUTPUT_ID) {
            return;
        }
        if (!(player instanceof ServerPlayerEntity sp) || player.getEntityWorld().isClient()) {
            return;
        }
        auraskills$grindstoneTrackingOutputClick = true;
        auraskills$outputBeforeClick = self.getSlot(GrindstoneScreenHandler.OUTPUT_ID).getStack().copy();
        auraskills$input1Before = self.getSlot(GrindstoneScreenHandler.INPUT_1_ID).getStack().copy();
        auraskills$input2Before = self.getSlot(GrindstoneScreenHandler.INPUT_2_ID).getStack().copy();
        auraskills$grindstoneCursorEmptyAtStart = self.getCursorStack().isEmpty();
        auraskills$grindstonePlayerStorageFull = FabricItemInventoryUtil.isPlayerStorageFull(sp.getInventory());
    }

    @Inject(method = "onSlotClick", at = @At("RETURN"))
    private void auraskills$grindstoneAward(int slotIndex, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        if (!((Object) this instanceof GrindstoneScreenHandler self)) {
            return;
        }
        try {
            if (!auraskills$grindstoneTrackingOutputClick) {
                return;
            }
            if (!(player instanceof ServerPlayerEntity sp) || player.getEntityWorld().isClient()) {
                return;
            }
            if (slotIndex != GrindstoneScreenHandler.OUTPUT_ID) {
                return;
            }
            ItemStack after = self.getSlot(GrindstoneScreenHandler.OUTPUT_ID).getStack();
            if (auraskills$outputBeforeClick.isEmpty()) {
                return;
            }
            if (!ItemStack.areItemsAndComponentsEqual(auraskills$outputBeforeClick, after)
                    || after.getCount() < auraskills$outputBeforeClick.getCount()) {
                if (!FabricGrindstoneXpHandler.allowsGrindstoneClickForXp(
                        actionType, auraskills$grindstoneCursorEmptyAtStart, auraskills$grindstonePlayerStorageFull)) {
                    return;
                }
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
