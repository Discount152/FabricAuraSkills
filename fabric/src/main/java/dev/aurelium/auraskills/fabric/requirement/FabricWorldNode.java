package dev.aurelium.auraskills.fabric.requirement;

import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.FabricUser;
import net.minecraft.server.level.ServerPlayer;

final class FabricWorldNode extends FabricRequirementNode {

    private final String world;

    FabricWorldNode(FabricAuraSkills plugin, String world, String message) {
        super(plugin, message);
        this.world = world;
    }

    @Override
    public boolean check(ServerPlayer player) {
        return world != null && FabricUser.worldNameCompat(player).equalsIgnoreCase(world);
    }
}
