package dev.granite.graniteskills.fabric.requirement;

import dev.granite.graniteskills.api.item.ModifierType;
import dev.granite.graniteskills.api.skill.Skill;
import net.minecraft.resources.Identifier;

import java.util.Map;

/** Global skill level requirement for a specific item id (Paper/Bukkit {@code GlobalRequirement} equivalent). */
public record FabricGlobalRequirement(ModifierType type, Identifier itemId, Map<Skill, Integer> requirements) {
}
