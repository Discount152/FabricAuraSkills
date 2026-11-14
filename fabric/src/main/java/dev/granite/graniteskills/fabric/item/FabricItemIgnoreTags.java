package dev.granite.graniteskills.fabric.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/**
 * Read/write Bukkit/Paper {@code graniteskills:ignore_interact} on {@link DataComponents#CUSTOM_DATA}
 * / {@code PublicBukkitValues} (same key as {@link dev.granite.graniteskills.bukkit.mana.ManaAbilityProvider} PDC).
 */
public final class FabricItemIgnoreTags {

    private static final String PUBLIC_BUKKIT_VALUES = "PublicBukkitValues";
    /** Bukkit {@code NamespacedKey(plugin, "ignore_interact")} → full PDC string. */
    public static final String IGNORE_INTERACT_KEY = "graniteskills:ignore_interact";

    private FabricItemIgnoreTags() {}

    public static boolean ignoresManaAbilityInteract(ItemStack stack) {
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
        if (el instanceof IntTag i) {
            return i.intValue() != 0;
        }
        return false;
    }

    /**
     * Sets or clears the ignore flag. When {@code false}, removes the key (if present).
     */
    public static void setIgnoreManaAbilityInteract(ItemStack stack, boolean on) {
        CompoundTag root;
        CustomData comp = stack.get(DataComponents.CUSTOM_DATA);
        if (comp == null) {
            root = new CompoundTag();
        } else {
            root = comp.copyTag();
        }
        CompoundTag pbv = root.getCompound(PUBLIC_BUKKIT_VALUES).orElseGet(CompoundTag::new);
        if (on) {
            pbv.put(IGNORE_INTERACT_KEY, ByteTag.ONE);
        } else {
            pbv.remove(IGNORE_INTERACT_KEY);
        }
        root.put(PUBLIC_BUKKIT_VALUES, pbv);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
    }
}
