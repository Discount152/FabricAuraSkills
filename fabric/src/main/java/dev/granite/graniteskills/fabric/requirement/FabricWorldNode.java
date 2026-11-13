package dev.granite.graniteskills.fabric.requirement;

import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import dev.granite.graniteskills.fabric.FabricUser;
import net.minecraft.server.level.ServerPlayer;

final class FabricWorldNode extends FabricRequirementNode {

    private final String world;

    FabricWorldNode(FabricGraniteSkills plugin, String world, String message) {
        super(plugin, message);
        this.world = world;
    }

    @Override
    public boolean check(ServerPlayer player) {
        return world != null && FabricUser.worldNameCompat(player).equalsIgnoreCase(world);
    }
}
