package dev.aurelium.auraskills.fabric.item;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Mirrors {@code ItemUtils.addItemToInventory} for main + hotbar (36 slots), like Bukkit storage contents.
 */
public final class FabricItemInventoryUtil {

    private FabricItemInventoryUtil() {
    }

    public static @Nullable ItemStack addItemToInventory(PlayerInventory inventory, ItemStack item) {
        if (item.isEmpty()) {
            return null;
        }
        int amountRemaining = item.getCount();
        for (int slot = 0; slot < 36; slot++) {
            ItemStack slotItem = inventory.getStack(slot);
            if (amountRemaining <= 0) {
                break;
            }
            if (slotItem.isEmpty()) {
                int max = item.getMaxCount();
                if (amountRemaining > max && max > 0) {
                    ItemStack placed = item.copyWithCount(max);
                    inventory.setStack(slot, placed);
                    amountRemaining -= max;
                } else {
                    ItemStack placed = item.copyWithCount(amountRemaining);
                    inventory.setStack(slot, placed);
                    amountRemaining = 0;
                }
            } else if (ItemStack.areItemsEqual(slotItem, item) && slotItem.getCount() < slotItem.getMaxCount()) {
                int room = slotItem.getMaxCount() - slotItem.getCount();
                int add = Math.min(amountRemaining, room);
                slotItem.increment(add);
                inventory.setStack(slot, slotItem);
                amountRemaining -= add;
            }
        }
        if (amountRemaining > 0) {
            return item.copyWithCount(amountRemaining);
        }
        return null;
    }
}
