package dev.granitelib.fabric.menu;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * 6×9 {@link MenuType#GENERIC_9x6} container. Top slots never allow item movement; when
 * {@code topClick != null}, it is notified for each container click (read-only + interaction).
 * When {@code topClick == null}, behavior matches the former read-only handler.
 *
 * <p>Visible top items are held on {@link DisplaySlot} instances. The backing
 * {@link GraniteDisplayContainer} is cleared and locked so inventory-management mods that walk
 * the {@link Container} API find nothing to take.
 */
public final class GraniteClickable9x6Handler extends ChestMenu {

    public static final int ROWS = 6;
    public static final int CONTAINER_SLOTS = ROWS * 9;

    private final @Nullable GraniteTopSlotClickHandler topClick;

    public GraniteClickable9x6Handler(
            int syncId,
            Inventory playerInventory,
            Container containerInventory,
            @Nullable GraniteTopSlotClickHandler topClick) {
        super(MenuType.GENERIC_9x6, syncId, playerInventory, containerInventory, ROWS);
        this.topClick = topClick;
        ItemStack[] shown = new ItemStack[CONTAINER_SLOTS];
        for (int i = 0; i < CONTAINER_SLOTS; i++) {
            shown[i] = containerInventory.getItem(i).copy();
        }
        if (containerInventory instanceof GraniteDisplayContainer display) {
            display.clearLocked();
            display.lock();
        }
        for (int i = 0; i < CONTAINER_SLOTS; i++) {
            Slot original = slots.get(i);
            DisplaySlot locked = new DisplaySlot(
                    containerInventory, original.getContainerSlot(), original.x, original.y, shown[i]);
            locked.index = i;
            slots.set(i, locked);
        }
    }

    /** Top-grid slot that shows a fixed stack and reports itself as neither takeable nor placeable. */
    private static final class DisplaySlot extends Slot {

        private final ItemStack display;

        private DisplaySlot(Container container, int containerSlot, int x, int y, ItemStack display) {
            super(container, containerSlot, x, y);
            this.display = display.isEmpty() ? ItemStack.EMPTY : display.copy();
        }

        @Override
        public ItemStack getItem() {
            return display.isEmpty() ? ItemStack.EMPTY : display.copy();
        }

        @Override
        public boolean hasItem() {
            return !display.isEmpty();
        }

        @Override
        public void set(ItemStack stack) {
            // Display only.
        }

        @Override
        public void setByPlayer(ItemStack stack, ItemStack previous) {
            // Display only.
        }

        @Override
        public ItemStack remove(int amount) {
            return ItemStack.EMPTY;
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean isHighlightable() {
            return true;
        }
    }

    @Override
    public void clicked(int slotIndex, int button, ContainerInput actionType, Player player) {
        // Block deposit into top slots via number-key swap / throw / drag as well as normal clicks.
        if (actionType == ContainerInput.SWAP && button >= 0 && button < CONTAINER_SLOTS) {
            return;
        }
        if (actionType == ContainerInput.QUICK_CRAFT && slotIndex >= 0 && slotIndex < CONTAINER_SLOTS) {
            return;
        }
        if (slotIndex >= 0 && slotIndex < CONTAINER_SLOTS) {
            if (actionType == ContainerInput.THROW
                    || actionType == ContainerInput.CLONE
                    || actionType == ContainerInput.PICKUP_ALL) {
                return;
            }
            if (topClick != null && player instanceof ServerPlayer sp) {
                topClick.onTopSlot(slotIndex, sp, button, actionType);
            }
            return;
        }
        super.clicked(slotIndex, button, actionType, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int fromIndex) {
        // Never move items into or out of the menu via shift-click; top slots are display/interaction only.
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        // Block double-click collect from pulling display items out of the top inventory.
        if (slot != null && slot.index >= 0 && slot.index < CONTAINER_SLOTS) {
            return false;
        }
        return super.canTakeItemForPickAll(stack, slot);
    }
}
