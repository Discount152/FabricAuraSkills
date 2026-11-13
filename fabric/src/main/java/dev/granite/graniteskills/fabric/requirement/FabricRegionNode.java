package dev.granite.graniteskills.fabric.requirement;

import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import net.minecraft.server.level.ServerPlayer;

final class FabricRegionNode extends FabricRequirementNode {

    FabricRegionNode(FabricGraniteSkills plugin, @SuppressWarnings("unused") String region, String message) {
        super(plugin, message);
    }

    @Override
    public boolean check(ServerPlayer player) {
        return true;
    }
}
