package dev.granite.graniteskills.bukkit.user;

import dev.granite.graniteskills.bukkit.GraniteSkills;
import dev.granite.graniteskills.common.ref.PlayerRef;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.common.user.UserManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static dev.granite.graniteskills.bukkit.ref.BukkitPlayerRef.unwrap;
import static dev.granite.graniteskills.bukkit.ref.BukkitPlayerRef.wrap;

public class BukkitUserManager extends UserManager {

    private final GraniteSkills plugin;

    public BukkitUserManager(GraniteSkills plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @NotNull
    public User getUser(Player player) {
        User user = playerDataMap.get(player.getUniqueId());
        if (user != null) {
            return user;
        } else {
            return createNewUser(player.getUniqueId(), wrap(player));
        }
    }

    @Override
    public User instantiateUser(UUID uuid, PlayerRef ref) {
        return new BukkitUser(uuid, ref != null ? unwrap(ref) : null, plugin);
    }

    @Override
    public List<User> getOnlineUsers() {
        List<User> online = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            User user = playerDataMap.get(player.getUniqueId());
            if (user != null) {
                online.add(user);
            }
        }
        return online;
    }

}
