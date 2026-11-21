package dev.granitelib.fabric.menu;

import net.minecraft.network.chat.Component;

import java.util.Objects;
import java.util.function.Function;

/**
 * Pluggable formatted-text parser for menu names/lore. Hosts (Granite Skills) install MiniMessage /
 * legacy-aware parsers; the default keeps plain literal text.
 */
public final class GraniteMenuText {

    private static volatile Function<String, Component> parser = Component::literal;

    private GraniteMenuText() {
    }

    public static void setParser(Function<String, Component> next) {
        parser = Objects.requireNonNullElse(next, Component::literal);
    }

    public static Component parse(String raw) {
        if (raw == null || raw.isEmpty()) {
            return Component.empty();
        }
        try {
            return parser.apply(raw);
        } catch (RuntimeException ignored) {
            return Component.literal(raw);
        }
    }
}
