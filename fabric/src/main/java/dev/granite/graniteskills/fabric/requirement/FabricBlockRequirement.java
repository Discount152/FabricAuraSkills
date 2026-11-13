package dev.granite.graniteskills.fabric.requirement;

import net.minecraft.resources.Identifier;

import java.util.List;

/**
 * Parsed {@code requirement.blocks.list} entry (Bukkit {@code BlockRequirement}).
 */
public record FabricBlockRequirement(
        Identifier blockId,
        boolean allowPlace,
        boolean allowBreak,
        boolean allowHarvest,
        List<FabricRequirementNode> nodes
) {
}
