package dev.aurelium.auraskills.fabric.command;

import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * Parses a Bukkit/ACF-style **language** token into a {@link Locale} for
 * {@link dev.aurelium.auraskills.common.message.MessageProvider#hasLocale(java.util.Locale)}.
 * Accepts BCP 47 (e.g. {@code en-US}, {@code zh-TW}) and common {@code en_us} style.
 */
public final class FabricCommandLocaleArgs {

    private FabricCommandLocaleArgs() {}

    @Nullable
    public static Locale parseLanguageTag(String raw) {
        if (raw == null || raw.isEmpty()) {
            return null;
        }
        String t = raw.trim();
        if (t.isEmpty()) {
            return null;
        }
        t = t.replace('_', '-');
        return Locale.forLanguageTag(t);
    }
}
