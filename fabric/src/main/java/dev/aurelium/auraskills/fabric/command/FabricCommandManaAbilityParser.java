package dev.aurelium.auraskills.fabric.command;

import dev.aurelium.auraskills.api.mana.ManaAbility;
import dev.aurelium.auraskills.api.registry.NamespacedId;
import dev.aurelium.auraskills.common.mana.ManaAbilityRegistry;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * Resolves a **enabled** {@link ManaAbility} from a single Brigadier token (Bukkit
 * {@code CommandRegistrar} {@code ManaAbility} context; {@link NamespacedId#fromDefault} for custom
 * ids). **Sprint 66.**
 */
public final class FabricCommandManaAbilityParser {

    private FabricCommandManaAbilityParser() {}

    @Nullable
    public static ManaAbility parse(FabricAuraSkills plugin, String raw) {
        if (raw == null || raw.isEmpty()) {
            return null;
        }
        ManaAbilityRegistry reg = plugin.getManaAbilityRegistry();
        ManaAbility byId = reg.getOrNull(NamespacedId.fromDefault(raw));
        if (byId != null) {
            return byId.isEnabled() ? byId : null;
        }
        String key = raw.toLowerCase(Locale.ROOT);
        for (ManaAbility a : reg.getValues()) {
            if (a.getId().getKey().equalsIgnoreCase(key)) {
                return a.isEnabled() ? a : null;
            }
        }
        return null;
    }
}
