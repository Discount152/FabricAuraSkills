package dev.granite.graniteskills.common.item;

/**
 * Whether a registered item may be given. An expiration of zero means the item does not expire.
 * An expiration at or before {@code now} means the item is not given.
 */
public final class ItemGrantExpiry {

    private ItemGrantExpiry() {
    }

    public static boolean shouldGive(long expirationEpochMillis, long nowEpochMillis) {
        return expirationEpochMillis <= 0L || nowEpochMillis < expirationEpochMillis;
    }

    public static int amountToGive(long expirationEpochMillis, long nowEpochMillis, int requestedAmount) {
        if (!shouldGive(expirationEpochMillis, nowEpochMillis) || requestedAmount <= 0) {
            return 0;
        }
        return requestedAmount;
    }
}
