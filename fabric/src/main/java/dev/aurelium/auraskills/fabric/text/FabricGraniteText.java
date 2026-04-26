package dev.aurelium.auraskills.fabric.text;

import java.util.Locale;
import net.minecraft.text.Text;
import net.minecraft.util.Language;

/**
 * Resolves mod lang keys to plain {@link Text#literal} on the server, using the
 * <strong>server-side</strong> language map. Vanilla clients (no FabricAuraSkills en_us assets)
 * cannot resolve {@link Text#translatable} for {@code auraskills.granite.*} and would see raw keys;
 * this path sends the already-resolved string in the text packet.
 */
public final class FabricGraniteText {

    private FabricGraniteText() {}

    /**
     * Returns literal text for a Granite/aura lang key, with {@link String#format} arguments when
     * the pattern uses {@code %s} / {@code %1$s} (same as vanilla format-style translations).
     */
    public static Text key(String key, Object... args) {
        // Language#get returns the key itself when there is no translation
        String pattern = Language.getInstance().get(key);
        if (pattern == null || pattern.isEmpty() || pattern.equals(key)) {
            return Text.literal(key);
        }
        if (args == null || args.length == 0) {
            return Text.literal(pattern);
        }
        try {
            return Text.literal(String.format(Locale.ROOT, pattern, args));
        } catch (Exception ignored) {
            return Text.literal(pattern);
        }
    }
}
