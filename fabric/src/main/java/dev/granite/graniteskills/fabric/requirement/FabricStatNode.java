package dev.granite.graniteskills.fabric.requirement;

import dev.granite.graniteskills.api.stat.Stat;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import net.minecraft.server.level.ServerPlayer;

final class FabricStatNode extends FabricRequirementNode {

    private final Stat stat;
    private final int value;

    FabricStatNode(FabricGraniteSkills plugin, Stat stat, int value, String message) {
        super(plugin, message);
        this.stat = stat;
        this.value = value;
    }

    @Override
    public boolean check(ServerPlayer player) {
        if (stat == null) {
            return true;
        }
        return plugin.getUserManager().getUser(player.getUUID()).getStatLevel(stat) >= value;
    }
}
