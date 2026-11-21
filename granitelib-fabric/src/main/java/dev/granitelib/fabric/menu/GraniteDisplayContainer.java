package dev.granitelib.fabric.menu;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Menu display contents. After {@link #lock()}, the container reports empty to every
 * {@link Container} API caller (including other mods' sort/take-all helpers). Visible items live
 * on {@link GraniteClickable9x6Handler}'s display slots, not in this inventory.
 */
final class GraniteDisplayContainer extends SimpleContainer {

    private boolean locked;

    GraniteDisplayContainer(int size) {
        super(size);
    }

    boolean isLocked() {
        return locked;
    }

    void lock() {
        locked = true;
    }

    /** Clears stored stacks even after {@link #lock()} so Container helpers see an empty chest. */
    void clearLocked() {
        for (int i = 0; i < getContainerSize(); i++) {
            super.setItem(i, ItemStack.EMPTY);
        }
    }

    @Override
    public ItemStack getItem(int slot) {
        return locked ? ItemStack.EMPTY : super.getItem(slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (!locked) {
            super.setItem(slot, stack);
        }
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return locked ? ItemStack.EMPTY : super.removeItem(slot, amount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return locked ? ItemStack.EMPTY : super.removeItemNoUpdate(slot);
    }

    @Override
    public ItemStack removeItemType(Item item, int amount) {
        return locked ? ItemStack.EMPTY : super.removeItemType(item, amount);
    }

    @Override
    public List<ItemStack> removeAllItems() {
        return locked ? List.of() : super.removeAllItems();
    }

    @Override
    public ItemStack addItem(ItemStack stack) {
        return locked ? stack : super.addItem(stack);
    }

    @Override
    public boolean canAddItem(ItemStack stack) {
        return !locked && super.canAddItem(stack);
    }

    @Override
    public void clearContent() {
        if (!locked) {
            super.clearContent();
        }
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return !locked;
    }

    @Override
    public boolean canTakeItem(Container target, int slot, ItemStack stack) {
        return !locked;
    }

    @Override
    public boolean isEmpty() {
        return locked || super.isEmpty();
    }
}
