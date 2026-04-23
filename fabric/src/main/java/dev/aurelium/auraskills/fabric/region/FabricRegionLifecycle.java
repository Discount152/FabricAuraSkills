package dev.aurelium.auraskills.fabric.region;

import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.chunk.WorldChunk;

/**
 * Chunk load + async save schedule for {@link FabricRegionManager} (Bukkit {@code RegionListener}).
 */
public final class FabricRegionLifecycle {

    private FabricRegionLifecycle() {
    }

    public static void register(FabricAuraSkills plugin) {
        FabricRegionManager regionManager = plugin.getFabricRegionManager();
        regionManager.startAsyncSaveTimer();
        ServerChunkEvents.CHUNK_LOAD.register((ServerWorld world, WorldChunk chunk) -> regionManager.onChunkLoad(world, chunk));
    }
}
