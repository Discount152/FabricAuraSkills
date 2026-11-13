package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.common.ref.LocationRef;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public final class FabricLocationRef implements LocationRef {

    private final Object level;
    private final String worldName;
    private final double x;
    private final double y;
    private final double z;

    public FabricLocationRef(ServerPlayer player) {
        this.level = player.level();
        this.worldName = FabricUser.worldNameCompat(player);
        this.x = player.getX();
        this.y = player.getY();
        this.z = player.getZ();
    }

    @Override
    public Object get() {
        return level;
    }

    @Override
    public Optional<String> getWorldName() {
        return Optional.of(worldName);
    }

    public double distanceSquared(FabricLocationRef other) {
        double dx = x - other.x;
        double dy = y - other.y;
        double dz = z - other.z;
        return dx * dx + dy * dy + dz * dz;
    }
}
