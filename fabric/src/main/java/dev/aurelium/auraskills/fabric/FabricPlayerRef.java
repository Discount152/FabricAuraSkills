package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.common.ref.LocationRef;
import dev.aurelium.auraskills.common.ref.PlayerRef;
import net.minecraft.server.level.ServerPlayer;

public record FabricPlayerRef(ServerPlayer player) implements PlayerRef {

    @Override
    public Object get() {
        return player;
    }

    @Override
    public LocationRef getLocation() {
        return new FabricLocationRef(player);
    }
}
