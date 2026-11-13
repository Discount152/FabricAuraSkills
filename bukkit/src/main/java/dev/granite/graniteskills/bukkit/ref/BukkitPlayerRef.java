package dev.granite.graniteskills.bukkit.ref;

import dev.granite.graniteskills.common.ref.LocationRef;
import dev.granite.graniteskills.common.ref.PlayerRef;
import org.bukkit.entity.Player;

public class BukkitPlayerRef implements PlayerRef {

    private final Player player;

    private BukkitPlayerRef(Player player) {
        this.player = player;
    }

    public static BukkitPlayerRef wrap(Player player) {
        return new BukkitPlayerRef(player);
    }

    public static Player unwrap(PlayerRef ref) {
        return ((BukkitPlayerRef) ref).get();
    }

    @Override
    public Player get() {
        return player;
    }

    @Override
    public LocationRef getLocation() {
        return BukkitLocationRef.wrap(player.getLocation());
    }

}
