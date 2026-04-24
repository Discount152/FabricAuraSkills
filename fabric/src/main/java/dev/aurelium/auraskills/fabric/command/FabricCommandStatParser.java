package dev.aurelium.auraskills.fabric.command;

import dev.aurelium.auraskills.api.registry.NamespacedId;
import dev.aurelium.auraskills.api.stat.Stat;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * Parses a **stat** id from a single Brigadier string token (analogous to
 * {@link FabricCommandSkillParser} for skills).
 */
public final class FabricCommandStatParser {

    private FabricCommandStatParser() {}

    /**
     * Resolves an enabled {@link Stat}, or null if unknown/disabled. Accepts
     * registry key (e.g. {@code strength}) and {@code namespace:key} for custom stats.
     */
    @Nullable
    public static Stat parse(FabricAuraSkills plugin, String raw) {
        if (raw == null || raw.isEmpty()) {
            return null;
        }
        String key = raw.toLowerCase(Locale.ROOT);
        for (Stat stat : plugin.getStatRegistry().getValues()) {
            if (stat.getId().getKey().equals(key)) {
                if (!stat.isEnabled()) {
                    return null;
                }
                return stat;
            }
        }
        Stat stat = plugin.getStatRegistry().getOrNull(NamespacedId.fromDefault(raw));
        if (stat == null || !stat.isEnabled()) {
            return null;
        }
        return stat;
    }
}
