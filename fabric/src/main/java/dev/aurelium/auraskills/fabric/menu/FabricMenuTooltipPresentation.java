package dev.aurelium.auraskills.fabric.menu;

import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.item.ItemStack;

import java.util.List;

/**
 * Sprint 114: hide vanilla item-mechanics lines from menu-only ItemStacks so tooltips show Granite name + lore only.
 */
public final class FabricMenuTooltipPresentation {

    @SuppressWarnings("rawtypes")
    private static final List<ComponentType<?>> HIDE_VANILLA_MECHANICS = List.of(
            DataComponentTypes.ATTRIBUTE_MODIFIERS,
            DataComponentTypes.TOOL,
            DataComponentTypes.MAX_DAMAGE,
            DataComponentTypes.DAMAGE,
            DataComponentTypes.TRIM,
            DataComponentTypes.POTION_CONTENTS,
            DataComponentTypes.BLOCKS_ATTACKS,
            DataComponentTypes.ENCHANTMENTS,
            DataComponentTypes.STORED_ENCHANTMENTS,
            DataComponentTypes.REPAIRABLE,
            DataComponentTypes.EQUIPPABLE,
            DataComponentTypes.WEAPON,
            DataComponentTypes.CONSUMABLE,
            DataComponentTypes.USE_REMAINDER,
            DataComponentTypes.USE_COOLDOWN,
            DataComponentTypes.FOOD);

    private FabricMenuTooltipPresentation() {
    }

    /** Sets TOOLTIP_DISPLAY while keeping ITEM_NAME + lore visible. */
    public static void suppressVanillaMechanicsTooltip(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        TooltipDisplayComponent display = TooltipDisplayComponent.DEFAULT;
        for (ComponentType<?> type : HIDE_VANILLA_MECHANICS) {
            display = display.with(type, true);
        }
        stack.set(DataComponentTypes.TOOLTIP_DISPLAY, display);
    }
}
