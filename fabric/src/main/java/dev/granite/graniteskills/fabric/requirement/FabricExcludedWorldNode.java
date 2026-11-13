package dev.granite.graniteskills.fabric.requirement;

import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import dev.granite.graniteskills.fabric.FabricUser;
import net.minecraft.server.level.ServerPlayer;

final class FabricExcludedWorldNode extends FabricRequirementNode {

    private final String[] worlds;

    FabricExcludedWorldNode(FabricGraniteSkills plugin, String[] worlds, String message) {
        super(plugin, message);
        this.worlds = worlds != null ? worlds : new String[0];
    }

    @Override
    public boolean check(ServerPlayer player) {
        String w = FabricUser.worldNameCompat(player);
        for (String world : worlds) {
            if (world != null && w.equalsIgnoreCase(world)) {
                return false;
            }
        }
        return true;
    }
}
