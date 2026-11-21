package dev.granite.graniteskills.fabric.item;

import dev.granite.graniteskills.common.item.ItemGrantExpiry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;

/**
 * Reads {@code expiration_time} from item custom data. The give path uses {@link ItemGrantExpiry}
 * on this value and does not add an expired stack to the inventory or the unclaimed list.
 */
public final class FabricItemExpiry {

    static final String EXPIRATION_TIME = "expiration_time";
    private static final String PUBLIC_BUKKIT_VALUES = "PublicBukkitValues";
    private static final String[] KEYS = {
            "graniteskills:expiration_time",
            "auraskills:expiration_time",
            "aureliumskills:expiration_time"
    };

    private FabricItemExpiry() {
    }

    public static long readExpiration(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return 0L;
        }
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) {
            return 0L;
        }
        return readExpiration(data.copyTag());
    }

    public static long readExpiration(CompoundTag customData) {
        if (customData == null || customData.isEmpty()) {
            return 0L;
        }
        long direct = customData.getLongOr(EXPIRATION_TIME, 0L);
        if (direct > 0L) {
            return direct;
        }
        CompoundTag values = customData.getCompound(PUBLIC_BUKKIT_VALUES).orElse(null);
        if (values == null) {
            return 0L;
        }
        for (String key : KEYS) {
            long value = values.getLongOr(key, 0L);
            if (value > 0L) {
                return value;
            }
        }
        return 0L;
    }

    public static void writeExpiration(ItemStack stack, long expirationEpochMillis) {
        if (stack == null || stack.isEmpty() || expirationEpochMillis <= 0L) {
            return;
        }
        CustomData existing = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag root = existing == null ? new CompoundTag() : existing.copyTag();
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(withExpiration(root, expirationEpochMillis)));
    }

    static CompoundTag withExpiration(CompoundTag root, long expirationEpochMillis) {
        CompoundTag values = root.getCompound(PUBLIC_BUKKIT_VALUES).orElseGet(CompoundTag::new);
        values.put(KEYS[0], LongTag.valueOf(expirationEpochMillis));
        root.put(PUBLIC_BUKKIT_VALUES, values);
        return root;
    }

    static int amountToGive(ItemStack stack, int resolvedCount, long nowEpochMillis) {
        return ItemGrantExpiry.amountToGive(readExpiration(stack), nowEpochMillis, resolvedCount);
    }
}
