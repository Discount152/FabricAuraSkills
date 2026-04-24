package dev.granitelib.fabric.menu;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;

/**
 * A {@value #ROWS}×9 chest-style menu using {@link ScreenHandlerType#GENERIC_9X6} so the vanilla client
 * and Geyser (large chest) see a standard container. Container slots (indices {@code 0..ROWS*9-1})
 * are read-only; player inventory and hotbar remain normal.
 */
public final class GraniteReadOnly9x6Handler extends GenericContainerScreenHandler {

    public static final int ROWS = 6;
    private static final int CONTAINER_SLOTS = ROWS * 9;

    public GraniteReadOnly9x6Handler(int syncId, PlayerInventory playerInventory, Inventory containerInventory) {
        super(ScreenHandlerType.GENERIC_9X6, syncId, playerInventory, containerInventory, ROWS);
    }

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        if (slotIndex >= 0 && slotIndex < CONTAINER_SLOTS) {
            // Block interaction with the top container (items are display-only; still synced from server)
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
