package dev.aurelium.auraskills.fabric.item;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtInt;

/**
 * Read/write Bukkit/Paper {@code auraskills:ignore_interact} on {@link DataComponentTypes#CUSTOM_DATA}
 * / {@code PublicBukkitValues} (same key as {@link dev.aurelium.auraskills.bukkit.mana.ManaAbilityProvider} PDC).
 */
public final class FabricItemIgnoreTags {

    private static final String PUBLIC_BUKKIT_VALUES = "PublicBukkitValues";
    /** Bukkit {@code NamespacedKey(plugin, "ignore_interact")} → full PDC string. */
    public static final String IGNORE_INTERACT_KEY = "auraskills:ignore_interact";

    private FabricItemIgnoreTags() {}

    public static boolean ignoresManaAbilityInteract(ItemStack stack) {
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
        if (el instanceof NbtInt i) {
            return i.intValue() != 0;
        }
        return false;
    }

    /**
     * Sets or clears the ignore flag. When {@code false}, removes the key (if present).
     */
    public static void setIgnoreManaAbilityInteract(ItemStack stack, boolean on) {
        NbtCompound root;
        NbtComponent comp = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (comp == null) {
            root = new NbtCompound();
        } else {
            root = comp.copyNbt();
        }
        NbtCompound pbv = root.getCompound(PUBLIC_BUKKIT_VALUES).orElseGet(NbtCompound::new);
        if (on) {
            pbv.put(IGNORE_INTERACT_KEY, NbtByte.ONE);
        } else {
            pbv.remove(IGNORE_INTERACT_KEY);
        }
        root.put(PUBLIC_BUKKIT_VALUES, pbv);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(root));
    }
}
