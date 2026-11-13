package dev.aurelium.auraskills.fabric.requirement;

import dev.aurelium.auraskills.api.stat.Stat;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import net.minecraft.server.level.ServerPlayer;

final class FabricStatNode extends FabricRequirementNode {

    private final Stat stat;
    private final int value;

    FabricStatNode(FabricAuraSkills plugin, Stat stat, int value, String message) {
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
