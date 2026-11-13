package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.common.ref.LocationRef;
import java.util.Optional;
import net.minecraft.server.level.ServerPlayer;

public final class FabricLocationRef implements LocationRef {

    private final ServerPlayer player;

    public FabricLocationRef(ServerPlayer player) {
        this.player = player;
    }

    @Override
    public Object get() {
        return player.level();
    }

    @Override
    public Optional<String> getWorldName() {
        return Optional.of(FabricUser.worldNameCompat(player));
    }
}
