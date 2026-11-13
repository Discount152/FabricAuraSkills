package dev.aurelium.auraskills.fabric.item;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Mirrors {@code ItemUtils.addItemToInventory} for main + hotbar (36 slots), like Bukkit storage contents.
 */
public final class FabricItemInventoryUtil {

    private FabricItemInventoryUtil() {
    }

    public static @Nullable ItemStack addItemToInventory(Inventory inventory, ItemStack item) {
        if (item.isEmpty()) {
            return null;
        }
        int amountRemaining = item.getCount();
        for (int slot = 0; slot < 36; slot++) {
            ItemStack slotItem = inventory.getItem(slot);
            if (amountRemaining <= 0) {
                break;
            }
            if (slotItem.isEmpty()) {
                int max = item.getMaxStackSize();
                if (amountRemaining > max && max > 0) {
                    ItemStack placed = item.copyWithCount(max);
                    inventory.setItem(slot, placed);
                    amountRemaining -= max;
                } else {
                    ItemStack placed = item.copyWithCount(amountRemaining);
                    inventory.setItem(slot, placed);
                    amountRemaining = 0;
                }
            } else if (ItemStack.isSameItem(slotItem, item) && slotItem.getCount() < slotItem.getMaxStackSize()) {
                int room = slotItem.getMaxStackSize() - slotItem.getCount();
                int add = Math.min(amountRemaining, room);
                slotItem.grow(add);
                inventory.setItem(slot, slotItem);
                amountRemaining -= add;
            }
        }
        if (amountRemaining > 0) {
            return item.copyWithCount(amountRemaining);
        }
        return null;
    }
}
