package dev.granite.graniteskills.fabric.command;

import dev.granite.graniteskills.api.registry.NamespacedId;
import dev.granite.graniteskills.api.stat.Stat;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
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
    public static Stat parse(FabricGraniteSkills plugin, String raw) {
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
