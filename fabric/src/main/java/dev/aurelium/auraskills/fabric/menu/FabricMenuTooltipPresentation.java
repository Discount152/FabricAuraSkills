package dev.aurelium.auraskills.fabric.menu;

import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.item.ItemStack;

import java.util.List;

/**
 * Menu-only tooltips: hide vanilla mechanics (Sprint 114) and strip leak-prone default components (Sprint 115) so
 * Granite name + lore stay clean (including advanced tooltips and food/durability defaults).
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

    /**
     * Removed from the stack (not only hidden in {@link DataComponentTypes#TOOLTIP_DISPLAY}) so advanced tooltips
     * and default item-derived lines (durability, food) cannot leak from built-in defaults.
     */
    @SuppressWarnings("rawtypes")
    private static final List<ComponentType<?>> STRIP_FROM_STACK_FOR_MENU = List.of(
            DataComponentTypes.MAX_DAMAGE,
            DataComponentTypes.DAMAGE,
            DataComponentTypes.FOOD,
            DataComponentTypes.CONSUMABLE,
            DataComponentTypes.USE_REMAINDER,
            DataComponentTypes.USE_COOLDOWN,
            DataComponentTypes.POTION_CONTENTS);

    private FabricMenuTooltipPresentation() {
    }

    /**
     * Mutates {@code stack}: clears leak-prone default components, then sets {@link DataComponentTypes#TOOLTIP_DISPLAY}
     * so other vanilla mechanic sections stay hidden while {@link DataComponentTypes#ITEM_NAME} and
     * {@link DataComponentTypes#LORE} still appear.
     */
    public static void suppressVanillaMechanicsTooltip(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        for (ComponentType<?> type : STRIP_FROM_STACK_FOR_MENU) {
            if (type != null) {
                stack.remove(type);
            }
        }
        TooltipDisplayComponent display = TooltipDisplayComponent.DEFAULT;
        for (ComponentType<?> type : HIDE_VANILLA_MECHANICS) {
            if (type != null) {
                display = display.with(type, true);
            }
        }
        stack.set(DataComponentTypes.TOOLTIP_DISPLAY, display);
    }
}