package dev.aurelium.auraskills.fabric.storage;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;

import java.nio.file.Path;

/**
 * Overworld **playerdata** directory for {@code .dat} files. **Sprint 57.**
 */
public final class FabricServerPlayerDataPaths {

    private FabricServerPlayerDataPaths() {}

    public static Path overworldPlayerData(MinecraftServer server) {
        return server.getSavePath(WorldSavePath.PLAYERDATA);
    }
}
