package dev.aurelium.auraskills.fabric.requirement;

import dev.aurelium.auraskills.api.item.ModifierType;
import dev.aurelium.auraskills.api.skill.Skill;
import java.util.Map;
import net.minecraft.resources.Identifier;

/** Global skill level requirement for a specific item id (Paper/Bukkit {@code GlobalRequirement} equivalent). */
public record FabricGlobalRequirement(ModifierType type, Identifier itemId, Map<Skill, Integer> requirements) {
}
