package dev.granite.graniteskills.fabric.menu;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

import java.util.List;

/**
 * Sprint 121D: single place for quiet fillers + paging item shape so menus do not
 * reimplement {@link FabricMenuTooltipPresentation#suppressVanillaMechanicsTooltip} ad hoc.
 */
public final class FabricMenuItemPresentation {

    private FabricMenuItemPresentation() {}

    /** Blank visible name (space) + mechanics suppression when a filler stack is unavoidable. */
    public static void applyQuietFiller(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        stack.set(DataComponents.ITEM_NAME, Component.literal(" "));
        FabricMenuTooltipPresentation.suppressVanillaMechanicsTooltip(stack);
    }

    /** Previous/next page control: arrow only (never back navigation). */
    public static ItemStack pageNavArrow(Component itemName, Component loreLine) {
        ItemStack s = new ItemStack(Items.ARROW);
        s.set(DataComponents.ITEM_NAME, itemName);
        s.set(DataComponents.LORE, new ItemLore(List.of(loreLine)));
        FabricMenuTooltipPresentation.suppressVanillaMechanicsTooltip(s);
        return s;
    }
}
