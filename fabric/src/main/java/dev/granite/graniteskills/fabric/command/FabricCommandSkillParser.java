package dev.granite.graniteskills.fabric.command;

import dev.granite.graniteskills.api.registry.NamespacedId;
import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
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
    public static Skill parse(FabricGraniteSkills plugin, String raw) {
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
