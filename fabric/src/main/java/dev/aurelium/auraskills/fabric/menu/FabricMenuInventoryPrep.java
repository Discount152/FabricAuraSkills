package dev.aurelium.auraskills.fabric.menu;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

/** Clears the top (menu) region of a clickable container inventory before templating. */
public final class FabricMenuInventoryPrep {

    private FabricMenuInventoryPrep() {}

    public static void clearAllSlots(Inventory inv) {
        for (int i = 0; i < FabricMenuSlotLayout.TOP_SLOTS; i++) {
            inv.setStack(i, ItemStack.EMPTY);
        }
    }
}