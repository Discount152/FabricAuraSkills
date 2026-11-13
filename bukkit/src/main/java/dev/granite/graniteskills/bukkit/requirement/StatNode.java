package dev.granite.graniteskills.bukkit.requirement;

import dev.granite.graniteskills.api.stat.Stat;
import dev.granite.graniteskills.bukkit.GraniteSkills;
import org.bukkit.entity.Player;

public class StatNode extends RequirementNode {

    private final Stat stat;
    private final int value;

    public StatNode(GraniteSkills plugin, Stat stat, int value, String message) {
        super(plugin, message);
        this.stat = stat;
        this.value = value;
    }

    @Override
    public boolean check(Player player) {
        return plugin.getUser(player).getStatLevel(stat) >= value;
    }

}
