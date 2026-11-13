package dev.granite.graniteskills.bukkit.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import dev.granite.graniteskills.bukkit.GraniteSkills;
import org.bukkit.entity.Player;

@CommandAlias("stats")
public class StatsCommand extends BaseCommand {

    private final GraniteSkills plugin;

    public StatsCommand(GraniteSkills plugin) {
        this.plugin = plugin;
    }

    @Default
    @CommandPermission("graniteskills.command.stats")
    @Description("%desc_stats")
    public void onStats(Player player) {
        plugin.getSlate().openMenu(player, "stats");
    }

}
