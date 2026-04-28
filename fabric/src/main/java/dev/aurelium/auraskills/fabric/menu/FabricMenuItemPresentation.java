package dev.aurelium.auraskills.fabric.menu;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

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
        stack.set(DataComponentTypes.ITEM_NAME, Text.literal(" "));
        FabricMenuTooltipPresentation.suppressVanillaMechanicsTooltip(stack);
    }

    /** Previous/next page control: arrow only (never back navigation). */
    public static ItemStack pageNavArrow(Text itemName, Text loreLine) {
        ItemStack s = new ItemStack(Items.ARROW);
        s.set(DataComponentTypes.ITEM_NAME, itemName);
        s.set(DataComponentTypes.LORE, new LoreComponent(List.of(loreLine)));
        FabricMenuTooltipPresentation.suppressVanillaMechanicsTooltip(s);
        return s;
    }
}