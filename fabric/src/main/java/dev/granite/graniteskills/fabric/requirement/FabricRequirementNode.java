package dev.granite.graniteskills.fabric.requirement;

import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import net.minecraft.server.level.ServerPlayer;

public abstract class FabricRequirementNode {

    protected final FabricGraniteSkills plugin;
    protected final String denyMessage;

    protected FabricRequirementNode(FabricGraniteSkills plugin, String denyMessage) {
        this.plugin = plugin;
        this.denyMessage = denyMessage != null ? denyMessage : "";
    }

    public abstract boolean check(ServerPlayer player);

    public String getDenyMessage() {
        return denyMessage;
    }
}
