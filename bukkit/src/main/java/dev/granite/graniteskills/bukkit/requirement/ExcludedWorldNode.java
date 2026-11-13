package dev.granite.graniteskills.bukkit.requirement;

import dev.granite.graniteskills.bukkit.GraniteSkills;
import org.bukkit.entity.Player;

public class ExcludedWorldNode extends RequirementNode {

    private final String[] worlds;

    public ExcludedWorldNode(GraniteSkills plugin, String[] worlds, String message) {
        super(plugin, message);
        this.worlds = worlds;
    }

    @Override
    public boolean check(Player player) {
        for (String world : worlds) {
            if (player.getWorld().getName().equalsIgnoreCase(world)) {
                return false;
            }
        }
        return true;
    }

}
