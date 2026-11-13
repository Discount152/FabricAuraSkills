package dev.granite.graniteskills.fabric;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Holds the ItemStack passed to the first {@code ItemEntity} spawned during
 * {@link net.minecraft.world.entity.projectile.FishingHook#retrieve} on the server thread.
 */
public final class FabricFishingCatchHolder {

    private static final ThreadLocal<ItemStack> PENDING = new ThreadLocal<>();

    private FabricFishingCatchHolder() {
    }

    public static void set(@Nullable ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            PENDING.remove();
        } else {
            PENDING.set(stack.copy());
        }
    }

    public static @Nullable ItemStack take() {
        ItemStack s = PENDING.get();
        PENDING.remove();
        return s;
    }

    public static void clear() {
        PENDING.remove();
    }
}
