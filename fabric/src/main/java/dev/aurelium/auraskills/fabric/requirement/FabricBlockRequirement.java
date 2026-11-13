package dev.aurelium.auraskills.fabric.requirement;

import java.util.List;
import net.minecraft.resources.Identifier;

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
