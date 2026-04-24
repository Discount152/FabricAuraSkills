package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.fabric.item.FabricItemIgnoreTags;
import net.minecraft.item.ItemStack;

/**
 * Reads Bukkit/Paper {@code ignore_interact} PDC from item {@code custom_data} for parity with
 * {@link dev.aurelium.auraskills.bukkit.mana.ManaAbilityProvider#shouldIgnoreItem}.
 */
final class FabricManaAbilityItemTags {

    private FabricManaAbilityItemTags() {
    }

    static boolean shouldIgnoreInteract(ItemStack stack) {
        return FabricItemIgnoreTags.ignoresManaAbilityInteract(stack);
    }
}
