package dev.granite.graniteskills.common.item;

import dev.granite.graniteskills.common.util.data.KeyIntPair;

import java.util.List;

/**
 * How many unclaimed items stay after a claim. Items already delivered are not left on the list,
 * so a later claim cannot give them again.
 */
public final class UnclaimedClaim {

    private UnclaimedClaim() {
    }

    public static int remainingAfterDelivery(int requested, int notFit) {
        if (requested <= 0 || notFit <= 0) {
            return 0;
        }
        if (notFit >= requested) {
            return requested;
        }
        return notFit;
    }

    public static int delivered(int requested, int notFit) {
        return Math.max(0, requested) - remainingAfterDelivery(requested, notFit);
    }

    /**
     * Applies one claim to the unclaimed list. Returns how many items this claim delivered.
     * A later call only sees what was not delivered.
     */
    public static int recordDelivery(List<KeyIntPair> unclaimed, int index, int notFit) {
        if (unclaimed == null || index < 0 || index >= unclaimed.size()) {
            return 0;
        }
        KeyIntPair pair = unclaimed.get(index);
        int requested = pair.getValue();
        int remaining = remainingAfterDelivery(requested, notFit);
        if (remaining == 0) {
            unclaimed.remove(index);
        } else if (remaining < requested) {
            pair.setValue(remaining);
        }
        return delivered(requested, notFit);
    }
}
