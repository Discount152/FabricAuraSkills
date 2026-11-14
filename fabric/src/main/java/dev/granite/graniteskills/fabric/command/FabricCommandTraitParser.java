package dev.granite.graniteskills.fabric.command;

import dev.granite.graniteskills.api.registry.NamespacedId;
import dev.granite.graniteskills.api.trait.Trait;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * Parses a **trait** id from a single Brigadier token (mirrors {@link FabricCommandStatParser}).
 */
public final class FabricCommandTraitParser {

    private FabricCommandTraitParser() {}

    @Nullable
    public static Trait parse(FabricGraniteSkills plugin, String raw) {
        if (raw == null || raw.isEmpty()) {
            return null;
        }
        String key = raw.toLowerCase(Locale.ROOT);
        for (Trait trait : plugin.getTraitRegistry().getValues()) {
            if (trait.getId().getKey().equals(key)) {
                if (!trait.isEnabled()) {
                    return null;
                }
                return trait;
            }
        }
        Trait trait = plugin.getTraitRegistry().getOrNull(NamespacedId.fromDefault(raw));
        if (trait == null || !trait.isEnabled()) {
            return null;
        }
        return trait;
    }
}
