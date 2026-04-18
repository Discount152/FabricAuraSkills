package dev.aurelium.auraskills.fabric.mixin;

import dev.aurelium.auraskills.fabric.FabricBrewingXpHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.BrewingStandScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * {@code TAKEOUT} brewing XP when a potion is taken from slots 0–2 (Paper {@code BrewingLeveler#onTakePotionOut}).
 * Targets {@link ScreenHandler#onSlotClick} like {@link ScreenHandlerGrindstoneXpMixin}.
 */
@Mixin(ScreenHandler.class)
public abstract class ScreenHandlerBrewingTakeoutXpMixin {

    @Unique
    private boolean auraskills$brewingTakeoutTracking;

    @Unique
    private int auraskills$brewingTakeoutSlot = -1;

    @Unique
    private ItemStack auraskills$brewingTakeoutBefore = ItemStack.EMPTY;

    @Inject(method = "onSlotClick", at = @At("HEAD"))
    private void auraskills$brewingTakeoutHead(
            int slotIndex, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        if (!((Object) this instanceof BrewingStandScreenHandler handler)) {
            return;
        }
        auraskills$brewingTakeoutTracking = false;
        auraskills$brewingTakeoutSlot = -1;
        auraskills$brewingTakeoutBefore = ItemStack.EMPTY;
        if (slotIndex < 0 || slotIndex > 2) {
            return;
        }
        if (!(player instanceof ServerPlayerEntity) || player.getEntityWorld().isClient()) {
            return;
        }
        auraskills$brewingTakeoutTracking = true;
        auraskills$brewingTakeoutSlot = slotIndex;
        auraskills$brewingTakeoutBefore = handler.getSlot(slotIndex).getStack().copy();
    }

    @Inject(method = "onSlotClick", at = @At("RETURN"))
    private void auraskills$brewingTakeoutReturn(
            int slotIndex, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        try {
            if (!auraskills$brewingTakeoutTracking || !(player instanceof ServerPlayerEntity sp)) {
                return;
            }
            if (!((Object) this instanceof BrewingStandScreenHandler handler)) {
                return;
            }
            if (slotIndex != auraskills$brewingTakeoutSlot) {
                return;
            }
            FabricBrewingXpHandler.onPotionSlotClickFinish(
                    sp, handler, slotIndex, actionType, auraskills$brewingTakeoutBefore);
        } finally {
            auraskills$brewingTakeoutTracking = false;
            auraskills$brewingTakeoutSlot = -1;
            auraskills$brewingTakeoutBefore = ItemStack.EMPTY;
        }
    }
}
