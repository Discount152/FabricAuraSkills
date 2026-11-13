package dev.aurelium.auraskills.fabric.requirement;

import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import net.minecraft.server.level.ServerPlayer;

final class FabricRegionNode extends FabricRequirementNode {

    FabricRegionNode(FabricAuraSkills plugin, @SuppressWarnings("unused") String region, String message) {
        super(plugin, message);
    }

    @Override
    public boolean check(ServerPlayer player) {
        return true;
    }
}
