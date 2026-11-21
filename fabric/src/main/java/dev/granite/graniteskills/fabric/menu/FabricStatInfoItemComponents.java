package dev.granite.graniteskills.fabric.menu;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;

import java.util.List;
import java.util.OptionalInt;

/**
 * Sprint 97: small ItemStack helpers for template-backed {@code stat_info} (components API, MC 1.21.11).
 */
public final class FabricStatInfoItemComponents {

    private FabricStatInfoItemComponents() {
    }

    /**
     * Applies legacy-style numeric custom model data via {@link CustomModelData} integer color list
     * (matches {@link dev.granite.graniteskills.fabric.FabricLootItemMatcher} / vanilla component shape).
     */
    public static void applyCustomModelData(ItemStack stack, OptionalInt customModelData) {
        if (customModelData.isEmpty()) {
            return;
        }
        int v = customModelData.getAsInt();
        stack.set(
                DataComponents.CUSTOM_MODEL_DATA,
                new CustomModelData(List.of(), List.of(), List.of(), List.of(v)));
    }
}
