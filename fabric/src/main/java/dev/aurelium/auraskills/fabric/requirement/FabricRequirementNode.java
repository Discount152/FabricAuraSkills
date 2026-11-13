package dev.aurelium.auraskills.fabric.requirement;

import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import net.minecraft.server.level.ServerPlayer;

public abstract class FabricRequirementNode {

    protected final FabricAuraSkills plugin;
    protected final String denyMessage;

    protected FabricRequirementNode(FabricAuraSkills plugin, String denyMessage) {
        this.plugin = plugin;
        this.denyMessage = denyMessage != null ? denyMessage : "";
    }

    public abstract boolean check(ServerPlayer player);

    public String getDenyMessage() {
        return denyMessage;
    }
}
