package dev.granite.graniteskills.fabric.menu;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.List;

/**
 * Menu-only tooltips: hide vanilla mechanics (Sprint 114) and strip leak-prone default components (Sprint 115) so
 * Granite name + lore stay clean (including advanced tooltips and food/durability defaults).
 */
public final class FabricMenuTooltipPresentation {

    @SuppressWarnings("rawtypes")
    private static final List<DataComponentType<?>> HIDE_VANILLA_MECHANICS = List.of(
            DataComponents.ATTRIBUTE_MODIFIERS,
            DataComponents.TOOL,
            DataComponents.MAX_DAMAGE,
            DataComponents.DAMAGE,
            DataComponents.TRIM,
            DataComponents.POTION_CONTENTS,
            DataComponents.BLOCKS_ATTACKS,
            DataComponents.ENCHANTMENTS,
            DataComponents.STORED_ENCHANTMENTS,
            DataComponents.REPAIRABLE,
            DataComponents.EQUIPPABLE,
            DataComponents.WEAPON,
            DataComponents.CONSUMABLE,
            DataComponents.USE_REMAINDER,
            DataComponents.USE_COOLDOWN,
            DataComponents.FOOD);

    /**
     * Removed from the stack (not only hidden in {@link DataComponents#TOOLTIP_DISPLAY}) so advanced tooltips
     * and default item-derived lines (durability, food) cannot leak from built-in defaults.
     */
    @SuppressWarnings("rawtypes")
    private static final List<DataComponentType<?>> STRIP_FROM_STACK_FOR_MENU = List.of(
            DataComponents.MAX_DAMAGE,
            DataComponents.DAMAGE,
            DataComponents.FOOD,
            DataComponents.CONSUMABLE,
            DataComponents.USE_REMAINDER,
            DataComponents.USE_COOLDOWN,
            DataComponents.POTION_CONTENTS);

    private FabricMenuTooltipPresentation() {
    }

    /**
     * Mutates {@code stack}: clears leak-prone default components, then sets {@link DataComponents#TOOLTIP_DISPLAY}
     * so other vanilla mechanic sections stay hidden while {@link DataComponents#ITEM_NAME} and
     * {@link DataComponents#LORE} still appear.
     */
    public static void suppressVanillaMechanicsTooltip(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        for (DataComponentType<?> type : STRIP_FROM_STACK_FOR_MENU) {
            if (type != null) {
                stack.remove(type);
            }
        }
        TooltipDisplay display = TooltipDisplay.DEFAULT;
        for (DataComponentType<?> type : HIDE_VANILLA_MECHANICS) {
            if (type != null) {
                display = display.withHidden(type, true);
            }
        }
        stack.set(DataComponents.TOOLTIP_DISPLAY, display);
    }
}
