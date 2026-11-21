package dev.granitelib.fabric.menu;

/**
 * Typed value and page passed to a menu opened by id.
 */
public record GraniteMenuContext<T>(T value, int page) {

    public GraniteMenuContext {
        if (page < 0) {
            throw new IllegalArgumentException("page must be >= 0");
        }
    }
}
