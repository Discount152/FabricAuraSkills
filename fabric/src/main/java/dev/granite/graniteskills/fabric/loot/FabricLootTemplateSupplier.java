package dev.granite.graniteskills.fabric.loot;

import dev.granite.graniteskills.common.loot.ItemSupplier;
import dev.granite.graniteskills.common.ref.ItemRef;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Resolves {@link ItemSupplier} to a template stack for loot-pool item matching (mirrors Bukkit
 * {@link dev.granite.graniteskills.bukkit.loot.item.BukkitItemSupplier#supplyItem} without random enchant rolls).
 */
public final class FabricLootTemplateSupplier {

    private FabricLootTemplateSupplier() {
    }

    public static @Nullable ItemStack supplyTemplate(ItemSupplier supplier) {
        ItemRef item = null;
        if (supplier.baseItem() != null) {
            item = supplier.baseItem().clone();
        }
        if (supplier.baseItemKey() != null) {
            return null;
        }
        if (item == null) {
            return null;
        }
        Object o = item.get();
        if (!(o instanceof ItemStack stack)) {
            return null;
        }
        return stack.copy();
    }
}
