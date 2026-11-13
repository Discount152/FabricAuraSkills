package dev.granite.graniteskills.bukkit.requirement;

import dev.granite.graniteskills.bukkit.GraniteSkills;
import org.bukkit.entity.Player;

public class WorldNode extends RequirementNode {

    private final String world;

    public WorldNode(GraniteSkills plugin, String world, String message) {
        super(plugin, message);
        this.world = world;
    }

    @Override
    public boolean check(Player player) {
        if (!player.getWorld().getName().equalsIgnoreCase(world)) {
            return false;
        }

        return true;
    }

}
