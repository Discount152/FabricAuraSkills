package dev.aurelium.auraskills.fabric.command;

import dev.aurelium.auraskills.api.registry.NamespacedId;
import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * Parses a **skill** id from a single Brigadier string token (reference: Bukkit
 * {@code CommandRegistrar} {@code Skill} context, {@code getFromKey} + {@link NamespacedId#fromDefault}).
 */
public final class FabricCommandSkillParser {

    private FabricCommandSkillParser() {}

    /**
     * Resolves an enabled {@link Skill}, or null if unknown/disabled. Accepts
     * registry key (e.g. {@code farming}) and {@code namespace:key} for custom skills.
     */
    @Nullable
    public static Skill parse(FabricAuraSkills plugin, String raw) {
        if (raw == null || raw.isEmpty()) {
            return null;
        }
        String key = raw.toLowerCase(Locale.ROOT);
        Skill skill = plugin.getSkillRegistry().getFromKey(key);
        if (skill == null) {
            skill = plugin.getSkillRegistry().getOrNull(NamespacedId.fromDefault(raw));
        }
        if (skill == null || !skill.isEnabled()) {
            return null;
        }
        return skill;
    }
}
