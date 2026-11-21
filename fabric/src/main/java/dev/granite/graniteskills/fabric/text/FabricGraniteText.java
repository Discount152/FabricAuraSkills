package dev.granite.graniteskills.fabric.text;

import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;

import java.util.Locale;

/**
 * Resolves mod lang keys to plain {@link Component#literal} on the server, using the
 * <strong>server-side</strong> language map. Vanilla clients (no FabricGraniteSkills en_us assets)
 * cannot resolve {@link Component#translatable} for {@code graniteskills.granite.*} and would see raw keys;
 * this path sends the already-resolved string in the text packet.
 */
public final class FabricGraniteText {

    private FabricGraniteText() {}

    /**
     * Returns literal text for a Granite/aura lang key, with {@link String#format} arguments when
     * the pattern uses {@code %s} / {@code %1$s} (same as vanilla format-style translations).
     */
    public static Component key(String key, Object... args) {
        // Language#get returns the key itself when there is no translation
        String pattern = Language.getInstance().getOrDefault(key);
        if (pattern == null || pattern.isEmpty() || pattern.equals(key)) {
            return Component.literal(key);
        }
        if (args == null || args.length == 0) {
            return Component.literal(pattern);
        }
        try {
            return Component.literal(String.format(Locale.ROOT, pattern, args));
        } catch (Exception ignored) {
            return Component.literal(pattern);
        }
    }
}
