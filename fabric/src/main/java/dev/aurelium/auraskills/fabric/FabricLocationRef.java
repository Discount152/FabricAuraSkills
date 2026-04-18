package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.common.ref.LocationRef;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Optional;

public final class FabricLocationRef implements LocationRef {

    private final ServerPlayerEntity player;

    public FabricLocationRef(ServerPlayerEntity player) {
        this.player = player;
    }

    @Override
    public Object get() {
        return player.getEntityWorld();
    }

    @Override
    public Optional<String> getWorldName() {
        return Optional.of(FabricUser.worldNameCompat(player));
    }
}
