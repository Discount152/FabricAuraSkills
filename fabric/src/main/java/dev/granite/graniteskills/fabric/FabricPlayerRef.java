package dev.granite.graniteskills.fabric;

import dev.granite.graniteskills.common.ref.LocationRef;
import dev.granite.graniteskills.common.ref.PlayerRef;
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
