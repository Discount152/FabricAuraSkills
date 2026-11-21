package dev.granite.graniteskills.fabric.storage;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.nio.file.Path;

/**
 * Overworld **playerdata** directory for {@code .dat} files. **Sprint 57.**
 */
public final class FabricServerPlayerDataPaths {

    private FabricServerPlayerDataPaths() {}

    public static Path overworldPlayerData(MinecraftServer server) {
        return server.getWorldPath(LevelResource.PLAYER_DATA_DIR);
    }
}
