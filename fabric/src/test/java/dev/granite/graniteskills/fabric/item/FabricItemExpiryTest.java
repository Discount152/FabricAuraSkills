package dev.granite.graniteskills.fabric.item;

import dev.granite.graniteskills.common.item.ItemGrantExpiry;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FabricItemExpiryTest {

    @Test
    void expiredItemIsNotGiven() {
        long now = 5_000L;
        CompoundTag expired = FabricItemExpiry.withExpiration(new CompoundTag(), now);
        assertEquals(0, ItemGrantExpiry.amountToGive(FabricItemExpiry.readExpiration(expired), now, 4));

        CompoundTag stillValid = FabricItemExpiry.withExpiration(new CompoundTag(), now + 1);
        assertEquals(4, ItemGrantExpiry.amountToGive(FabricItemExpiry.readExpiration(stillValid), now, 4));

        assertEquals(2, ItemGrantExpiry.amountToGive(FabricItemExpiry.readExpiration(new CompoundTag()), now, 2));
    }

    @Test
    void readsLegacyNamespacedExpiration() {
        CompoundTag values = new CompoundTag();
        values.putLong("auraskills:expiration_time", 80L);
        CompoundTag root = new CompoundTag();
        root.put("PublicBukkitValues", values);
        assertEquals(80L, FabricItemExpiry.readExpiration(root));
        assertEquals(0, ItemGrantExpiry.amountToGive(80L, 80L, 1));
    }
}
