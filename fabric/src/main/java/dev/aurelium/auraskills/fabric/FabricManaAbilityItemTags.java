package dev.aurelium.auraskills.fabric;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

/**
 * Reads Bukkit/Paper {@code ignore_interact} PDC from item {@code custom_data} for parity with
 * {@link dev.aurelium.auraskills.bukkit.mana.ManaAbilityProvider#shouldIgnoreItem}.
 */
final class FabricManaAbilityItemTags {

    private static final String PUBLIC_BUKKIT_VALUES = "PublicBukkitValues";
    /** Key string Paper uses for plugin PDC namespaced keys. */
    private static final String IGNORE_INTERACT_KEY = "auraskills:ignore_interact";

    private FabricManaAbilityItemTags() {
    }

    static boolean shouldIgnoreInteract(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        NbtComponent comp = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (comp == null) {
            return false;
        }
        NbtCompound root = comp.copyNbt();
        if (!root.contains(PUBLIC_BUKKIT_VALUES)) {
            return false;
        }
        NbtCompound pbv = root.getCompound(PUBLIC_BUKKIT_VALUES).orElse(null);
        if (pbv == null || !pbv.contains(IGNORE_INTERACT_KEY)) {
            return false;
        }
        NbtElement el = pbv.get(IGNORE_INTERACT_KEY);
        if (el instanceof NbtByte b) {
            return b.byteValue() != 0;
        }
        if (el instanceof net.minecraft.nbt.NbtInt i) {
            return i.intValue() != 0;
        }
        return false;
    }
}
