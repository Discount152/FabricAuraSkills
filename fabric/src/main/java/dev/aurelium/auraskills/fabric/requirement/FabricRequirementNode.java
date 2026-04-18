package dev.aurelium.auraskills.fabric.requirement;

import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import net.minecraft.server.network.ServerPlayerEntity;

public abstract class FabricRequirementNode {

    protected final FabricAuraSkills plugin;
    protected final String denyMessage;

    protected FabricRequirementNode(FabricAuraSkills plugin, String denyMessage) {
        this.plugin = plugin;
        this.denyMessage = denyMessage != null ? denyMessage : "";
    }

    public abstract boolean check(ServerPlayerEntity player);

    public String getDenyMessage() {
        return denyMessage;
    }
}
