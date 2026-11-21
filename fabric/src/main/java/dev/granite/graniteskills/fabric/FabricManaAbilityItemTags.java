package dev.granite.graniteskills.fabric;

import dev.granite.graniteskills.fabric.item.FabricItemIgnoreTags;
import net.minecraft.world.item.ItemStack;

/**
 * Reads Bukkit/Paper {@code ignore_interact} PDC from item {@code custom_data} for parity with
 * {@link dev.granite.graniteskills.bukkit.mana.ManaAbilityProvider#shouldIgnoreItem}.
 */
final class FabricManaAbilityItemTags {

    private FabricManaAbilityItemTags() {
    }

    static boolean shouldIgnoreInteract(ItemStack stack) {
        return FabricItemIgnoreTags.ignoresManaAbilityInteract(stack);
    }
}
