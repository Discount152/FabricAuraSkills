package dev.granitelib.fabric.menu;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

/**
 * 6×9 {@link ScreenHandlerType#GENERIC_9X6} container. Top slots never allow item movement; when
 * {@code topClick != null}, it is notified for each container click (read-only + interaction).
 * When {@code topClick == null}, behavior matches the former read-only handler.
 */
public final class GraniteClickable9x6Handler extends GenericContainerScreenHandler {

    public static final int ROWS = 6;
    public static final int CONTAINER_SLOTS = ROWS * 9;

    private final @Nullable GraniteTopSlotClickHandler topClick;

    public GraniteClickable9x6Handler(
            int syncId,
            PlayerInventory playerInventory,
            Inventory containerInventory,
            @Nullable GraniteTopSlotClickHandler topClick) {
        super(ScreenHandlerType.GENERIC_9X6, syncId, playerInventory, containerInventory, ROWS);
        this.topClick = topClick;
    }

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        if (slotIndex >= 0 && slotIndex < CONTAINER_SLOTS) {
            if (topClick != null && player instanceof ServerPlayerEntity sp) {
                topClick.onTopSlot(slotIndex, sp, button, actionType);
            }
            return;
        }
        super.onSlotClick(slotIndex, button, actionType, player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int fromIndex) {
        if (fromIndex >= 0 && fromIndex < CONTAINER_SLOTS) {
            return ItemStack.EMPTY;
        }
        return super.quickMove(player, fromIndex);
    }
}
