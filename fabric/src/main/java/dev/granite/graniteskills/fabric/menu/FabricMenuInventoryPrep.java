package dev.granite.graniteskills.fabric.menu;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

/** Clears the top (menu) region of a clickable container inventory before templating. */
public final class FabricMenuInventoryPrep {

    private FabricMenuInventoryPrep() {}

    public static void clearAllSlots(Container inv) {
        for (int i = 0; i < FabricMenuSlotLayout.TOP_SLOTS; i++) {
            inv.setItem(i, ItemStack.EMPTY);
        }
    }
}
