package dev.granite.graniteskills.fabric.loot;

import dev.granite.graniteskills.api.loot.LootRequirements;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import dev.granite.graniteskills.fabric.requirement.FabricRequirementNode;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.UUID;

public final class FabricNodeLootRequirements extends LootRequirements {

    private final FabricGraniteSkills plugin;
    private final List<FabricRequirementNode> nodes;

    public FabricNodeLootRequirements(FabricGraniteSkills plugin, List<FabricRequirementNode> nodes) {
        this.plugin = plugin;
        this.nodes = List.copyOf(nodes);
    }

    @Override
    public boolean checkByUuid(UUID uuid) {
        if (nodes.isEmpty()) {
            return true;
        }
        return check(plugin.getServer().getPlayerList().getPlayer(uuid));
    }

    public boolean check(ServerPlayer player) {
        if (nodes.isEmpty()) {
            return true;
        }
        if (player == null) {
            return false;
        }
        for (FabricRequirementNode node : nodes) {
            if (!node.check(player)) {
                return false;
            }
        }
        return true;
    }
}
