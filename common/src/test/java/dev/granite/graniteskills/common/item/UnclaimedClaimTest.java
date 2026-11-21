package dev.granite.graniteskills.common.item;

import dev.granite.graniteskills.common.util.data.KeyIntPair;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UnclaimedClaimTest {

    @Test
    void claimingDoesNotDuplicateStacksAlreadyDelivered() {
        List<KeyIntPair> unclaimed = new ArrayList<>();
        unclaimed.add(new KeyIntPair("graniteskills:gem", 10));

        int firstDelivered = apply(unclaimed, 0, 6);
        assertEquals(4, firstDelivered);
        assertEquals(6, unclaimed.get(0).getValue());

        int secondDelivered = apply(unclaimed, 0, 6);
        assertEquals(0, secondDelivered);
        assertEquals(6, unclaimed.get(0).getValue());

        int thirdDelivered = apply(unclaimed, 0, 0);
        assertEquals(6, thirdDelivered);
        assertTrue(unclaimed.isEmpty());
        assertEquals(10, firstDelivered + secondDelivered + thirdDelivered);
    }

    @Test
    void aSecondClaimOfAFullyDeliveredStackGivesNothing() {
        List<KeyIntPair> unclaimed = new ArrayList<>();
        unclaimed.add(new KeyIntPair("graniteskills:gem", 5));

        assertEquals(5, apply(unclaimed, 0, 0));
        assertTrue(unclaimed.isEmpty());
        assertEquals(0, apply(unclaimed, 0, 0));
    }

    private static int apply(List<KeyIntPair> unclaimed, int index, int notFit) {
        return UnclaimedClaim.recordDelivery(unclaimed, index, notFit);
    }
}
