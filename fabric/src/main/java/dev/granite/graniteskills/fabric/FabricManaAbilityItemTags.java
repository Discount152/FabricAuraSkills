package dev.granite.graniteskills.fabric;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/**
 * Reads Bukkit/Paper {@code ignore_interact} PDC from item {@code custom_data} for parity with
 * {@link dev.granite.graniteskills.bukkit.mana.ManaAbilityProvider#shouldIgnoreItem}.
 */
final class FabricManaAbilityItemTags {

    private static final String PUBLIC_BUKKIT_VALUES = "PublicBukkitValues";
    /** Key string Paper uses for plugin PDC namespaced keys. */
    private static final String IGNORE_INTERACT_KEY = "graniteskills:ignore_interact";

    private FabricManaAbilityItemTags() {
    }

    static boolean shouldIgnoreInteract(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        CustomData comp = stack.get(DataComponents.CUSTOM_DATA);
        if (comp == null) {
            return false;
        }
        CompoundTag root = comp.copyTag();
        if (!root.contains(PUBLIC_BUKKIT_VALUES)) {
            return false;
        }
        CompoundTag pbv = root.getCompound(PUBLIC_BUKKIT_VALUES).orElse(null);
        if (pbv == null || !pbv.contains(IGNORE_INTERACT_KEY)) {
            return false;
        }
        Tag el = pbv.get(IGNORE_INTERACT_KEY);
        if (el instanceof ByteTag b) {
            return b.byteValue() != 0;
        }
        if (el instanceof net.minecraft.nbt.IntTag i) {
            return i.intValue() != 0;
        }
        return false;
    }
}
