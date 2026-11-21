package dev.granite.graniteskills.common.item;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemGrantExpiryTest {

    @Test
    void expiredItemIsNotGiven() {
        long now = 1_700_000_000_000L;
        assertEquals(0, ItemGrantExpiry.amountToGive(now, now, 4));
        assertEquals(0, ItemGrantExpiry.amountToGive(now - 1, now, 4));
        assertFalse(ItemGrantExpiry.shouldGive(now, now));
    }

    @Test
    void futureAndUnsetItemsAreGiven() {
        long now = 1_700_000_000_000L;
        assertEquals(4, ItemGrantExpiry.amountToGive(now + 1, now, 4));
        assertEquals(3, ItemGrantExpiry.amountToGive(0L, now, 3));
        assertTrue(ItemGrantExpiry.shouldGive(0L, now));
        assertEquals(0, ItemGrantExpiry.amountToGive(now + 1, now, 0));
    }
}
