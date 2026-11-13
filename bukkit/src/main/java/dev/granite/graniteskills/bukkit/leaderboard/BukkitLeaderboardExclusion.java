package dev.granite.graniteskills.bukkit.leaderboard;

import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import dev.granite.graniteskills.common.leaderboard.LeaderboardExclusion;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.concurrent.TimeUnit;

public class BukkitLeaderboardExclusion extends LeaderboardExclusion implements Listener {

    public static final String PERMISSION = "graniteskills.leaderboard.exclude";

    public BukkitLeaderboardExclusion(GraniteSkillsPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.getScheduler().scheduleSync(() -> {
            Player player = event.getPlayer();
            if (player.hasPermission(PERMISSION)) {
                addExcludedPlayer(player.getUniqueId());
            } else {
                removeExcludedPlayer(player.getUniqueId());
            }
        }, 1, TimeUnit.SECONDS);
    }

}
