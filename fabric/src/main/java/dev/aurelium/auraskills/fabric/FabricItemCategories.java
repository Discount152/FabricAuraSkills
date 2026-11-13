package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.api.item.ItemCategory;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Approximates Bukkit {@code BukkitItemRegistry#getItemCategories} using registry item ids.
 */
final class FabricItemCategories {

    private FabricItemCategories() {
    }

    static Set<ItemCategory> categoriesFor(ItemStack stack) {
        if (stack.isEmpty()) {
            return Collections.emptySet();
        }
        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        EnumSet<ItemCategory> found = EnumSet.noneOf(ItemCategory.class);

        if (path.endsWith("_sword") || path.equals("bow") || path.equals("crossbow") || path.equals("trident")) {
            found.add(ItemCategory.WEAPON);
        } else if (path.endsWith("_helmet") || path.endsWith("_chestplate") || path.endsWith("_leggings")
                || path.endsWith("_boots") || path.equals("elytra")) {
            found.add(ItemCategory.ARMOR);
        } else if (path.endsWith("_pickaxe") || path.endsWith("_axe") || path.endsWith("_shovel")
                || path.endsWith("_hoe") || path.equals("shears") || path.equals("fishing_rod")
                || path.equals("flint_and_steel")) {
            found.add(ItemCategory.TOOL);
        }

        if (path.equals("lily_pad") || path.equals("bowl") || path.equals("leather") || path.equals("leather_boots")
                || path.equals("rotten_flesh") || path.equals("stick") || path.equals("string")
                || path.equals("bone") || path.equals("ink_sac") || path.equals("tripwire_hook")) {
            found.add(ItemCategory.FISHING_JUNK);
        }
        if (path.equals("fishing_rod")) {
            if (stack.isEnchanted()) {
                found.add(ItemCategory.FISHING_TREASURE);
            } else {
                found.add(ItemCategory.FISHING_JUNK);
            }
        }
        if (path.equals("potion")) {
            found.add(ItemCategory.FISHING_JUNK);
        }
        if (path.equals("bow") || path.equals("enchanted_book") || path.equals("name_tag")
                || path.equals("nautilus_shell") || path.equals("saddle")) {
            found.add(ItemCategory.FISHING_TREASURE);
        }

        return found.isEmpty() ? Collections.emptySet() : found;
    }
}
