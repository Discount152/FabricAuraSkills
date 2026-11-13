package dev.granite.graniteskills.bukkit.requirement;

import dev.granite.graniteskills.bukkit.GraniteSkills;
import org.bukkit.entity.Player;

public abstract class RequirementNode {

    protected GraniteSkills plugin;
    protected String denyMessage;

    public RequirementNode(GraniteSkills plugin, String denyMessage) {
        this.plugin = plugin;
        this.denyMessage = denyMessage;
    }

    public abstract boolean check(Player player);

    public String getDenyMessage() {
        return denyMessage;
    }

}
