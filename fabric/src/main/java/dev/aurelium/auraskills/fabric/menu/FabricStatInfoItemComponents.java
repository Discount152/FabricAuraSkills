package dev.aurelium.auraskills.fabric.menu;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.OptionalInt;

/**
 * Sprint 97: small ItemStack helpers for template-backed {@code stat_info} (components API, MC 1.21.11).
 */
public final class FabricStatInfoItemComponents {

    private FabricStatInfoItemComponents() {
    }

    /**
     * Applies legacy-style numeric custom model data via {@link CustomModelDataComponent} integer color list
     * (matches {@link dev.aurelium.auraskills.fabric.FabricLootItemMatcher} / vanilla component shape).
     */
    public static void applyCustomModelData(ItemStack stack, OptionalInt customModelData) {
        if (customModelData.isEmpty()) {
            return;
        }
        int v = customModelData.getAsInt();
        stack.set(
                DataComponentTypes.CUSTOM_MODEL_DATA,
                new CustomModelDataComponent(List.of(), List.of(), List.of(), List.of(v)));
    }
}
