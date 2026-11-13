package dev.granite.graniteskills.bukkit.antiafk;

import dev.granite.graniteskills.common.ref.LocationRef;
import dev.granite.graniteskills.common.ref.PlayerRef;

import static dev.granite.graniteskills.bukkit.ref.BukkitLocationRef.unwrap;
import static dev.granite.graniteskills.bukkit.ref.BukkitPlayerRef.unwrap;

public class HandlerFunctions {

    public static float getYaw(PlayerRef ref) {
        return unwrap(ref).getLocation().getYaw();
    }

    public static float getPitch(PlayerRef ref) {
        return unwrap(ref).getLocation().getPitch();
    }

    public static double distanceSquared(LocationRef ref1, LocationRef ref2) {
        return unwrap(ref1).distanceSquared(unwrap(ref2));
    }

}
