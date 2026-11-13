package dev.aurelium.auraskills.fabric.item;

import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.Set;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/**
 * Tracks last-known equipment stacks for item modifier application (Bukkit {@code UserEquipment}).
 */
public final class FabricUserEquipment {

    public static final Set<EquipmentSlot> TRACKED = Set.of(
            EquipmentSlot.MAINHAND,
            EquipmentSlot.OFFHAND,
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET
    );

    private final EnumMap<EquipmentSlot, ItemStack> slots = new EnumMap<>(EquipmentSlot.class);

    private FabricUserEquipment() {
        for (EquipmentSlot s : TRACKED) {
            slots.put(s, ItemStack.EMPTY);
        }
    }

    public static FabricUserEquipment empty() {
        return new FabricUserEquipment();
    }

    public @NotNull ItemStack getSlot(EquipmentSlot slot) {
        return slots.getOrDefault(slot, ItemStack.EMPTY).copy();
    }

    public void setSlot(EquipmentSlot slot, @NotNull ItemStack item) {
        if (!TRACKED.contains(slot)) {
            return;
        }
        slots.put(slot, item.copy());
    }
}
