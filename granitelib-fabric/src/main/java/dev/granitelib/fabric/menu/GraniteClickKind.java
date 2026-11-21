package dev.granitelib.fabric.menu;

import net.minecraft.world.inventory.ContainerInput;

/**
 * The click kinds a menu implementation must handle. Shift, number-key, drag, drop, and
 * double-click stay distinct so a caller cannot treat every click as a normal pickup.
 */
public enum GraniteClickKind {
    PICKUP,
    QUICK_MOVE,
    SWAP,
    CLONE,
    THROW,
    QUICK_CRAFT,
    PICKUP_ALL,
    OTHER;

    public static GraniteClickKind from(ContainerInput input) {
        if (input == null) {
            return OTHER;
        }
        return switch (input) {
            case PICKUP -> PICKUP;
            case QUICK_MOVE -> QUICK_MOVE;
            case SWAP -> SWAP;
            case CLONE -> CLONE;
            case THROW -> THROW;
            case QUICK_CRAFT -> QUICK_CRAFT;
            case PICKUP_ALL -> PICKUP_ALL;
        };
    }
}
