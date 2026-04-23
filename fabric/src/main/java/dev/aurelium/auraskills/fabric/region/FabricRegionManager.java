package dev.aurelium.auraskills.fabric.region;

import dev.aurelium.auraskills.api.source.SkillSource;
import dev.aurelium.auraskills.api.source.type.BlockXpSource;
import dev.aurelium.auraskills.common.region.BlockPosition;
import dev.aurelium.auraskills.common.region.ChunkCoordinate;
import dev.aurelium.auraskills.common.region.ChunkData;
import dev.aurelium.auraskills.common.region.Region;
import dev.aurelium.auraskills.common.region.RegionCoordinate;
import dev.aurelium.auraskills.common.config.Option;
import dev.aurelium.auraskills.common.region.RegionManager;
import dev.aurelium.auraskills.common.scheduler.TaskRunnable;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.FabricBlockXpSourceLookup;
import dev.aurelium.auraskills.fabric.FabricUser;
import dev.aurelium.auraskills.fabric.FabricWorldManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;

/**
 * Bukkit {@code BukkitRegionManager} on Fabric: same region/chunk NBT model as the common
 * {@link RegionManager} (persistence under {@code regiondata/}), driving {@code check_replace} behavior.
 */
public final class FabricRegionManager extends RegionManager {

    private final FabricAuraSkills plugin;

    public FabricRegionManager(FabricAuraSkills plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @Override
    public boolean isChunkLoaded(String worldName, int chunkX, int chunkZ) {
        for (ServerWorld w : plugin.getServer().getWorlds()) {
            if (FabricUser.worldNameCompat(w).equals(worldName) && w.getChunkManager().isChunkLoaded(chunkX, chunkZ)) {
                return true;
            }
        }
        return false;
    }

    public boolean isPlacedBlock(ServerWorld world, BlockPos pos) {
        int chunkX = pos.getX() >> 4;
        int chunkZ = pos.getZ() >> 4;
        int regionX = (int) Math.floor((double) chunkX / 32.0);
        int regionZ = (int) Math.floor((double) chunkZ / 32.0);
        String worldName = FabricUser.worldNameCompat(world);
        Region region = regions.get(new RegionCoordinate(worldName, regionX, regionZ));
        if (region != null) {
            byte regionChunkX = (byte) (chunkX - regionX * 32);
            byte regionChunkZ = (byte) (chunkZ - regionZ * 32);
            ChunkData chunkData = region.getChunkData(new ChunkCoordinate(regionChunkX, regionChunkZ));
            if (chunkData != null) {
                return chunkData.isPlacedBlock(new BlockPosition(pos.getX(), pos.getY(), pos.getZ()));
            }
        }
        return false;
    }

    public void handleBlockPlace(ServerWorld world, BlockPos pos, net.minecraft.block.BlockState state) {
        if (!plugin.isRuntimeReady()) {
            return;
        }
        if (!plugin.configBoolean(Option.CHECK_BLOCK_REPLACE_ENABLED)) {
            return;
        }
        if (plugin.getWorldManager() instanceof FabricWorldManager fwm && fwm.isCheckReplaceDisabled(world)) {
            return;
        }
        SkillSource<BlockXpSource> skillSource = FabricBlockXpSourceLookup.findForBreak(plugin, state);
        if (skillSource == null) {
            return;
        }
        BlockXpSource source = skillSource.source();
        if (!source.checkReplace()) {
            return;
        }
        addPlacedBlock(world, pos);
    }

    /**
     * Bukkit {@code BukkitRegionManager#addPlacedBlock} — for other mods that need to mark a block
     * without going through {@link #handleBlockPlace}.
     */
    public void addPlacedBlock(ServerWorld world, BlockPos pos) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        int chunkX = x >> 4;
        int chunkZ = z >> 4;
        int regionX = (int) Math.floor((double) chunkX / 32.0);
        int regionZ = (int) Math.floor((double) chunkZ / 32.0);
        String worldName = FabricUser.worldNameCompat(world);
        Region region = getRegionByCoords(worldName, regionX, regionZ);
        if (region == null || region.shouldReload()) {
            plugin.getScheduler().executeAsync(() -> {
                Region r = getRegionByCoords(worldName, regionX, regionZ);
                if (r == null) {
                    r = new Region(worldName, regionX, regionZ);
                    regions.put(new RegionCoordinate(worldName, regionX, regionZ), r);
                }
                loadRegion(r);
                addToRegion(r, x, y, z, chunkX, chunkZ, regionX, regionZ);
            });
        } else {
            addToRegion(region, x, y, z, chunkX, chunkZ, regionX, regionZ);
        }
    }

    public void removePlacedBlock(ServerWorld world, BlockPos pos) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        int chunkX = x >> 4;
        int chunkZ = z >> 4;
        int regionX = (int) Math.floor((double) chunkX / 32.0);
        int regionZ = (int) Math.floor((double) chunkZ / 32.0);
        String worldName = FabricUser.worldNameCompat(world);
        Region region = getRegionByCoords(worldName, regionX, regionZ);
        if (region != null) {
            byte regionChunkX = (byte) (chunkX - regionX * 32);
            byte regionChunkZ = (byte) (chunkZ - regionZ * 32);
            ChunkData chunkData = region.getChunkData(new ChunkCoordinate(regionChunkX, regionChunkZ));
            if (chunkData != null) {
                chunkData.removePlacedBlock(new BlockPosition(x, y, z));
            }
        }
    }

    private void addToRegion(
            Region region,
            int x, int y, int z,
            int chunkX, int chunkZ,
            int regionX, int regionZ) {
        byte regionChunkX = (byte) (chunkX - regionX * 32);
        byte regionChunkZ = (byte) (chunkZ - regionZ * 32);
        ChunkData chunkData = region.getChunkData(new ChunkCoordinate(regionChunkX, regionChunkZ));
        if (chunkData == null) {
            chunkData = new ChunkData(region, regionChunkX, regionChunkZ);
            region.setChunkData(new ChunkCoordinate(regionChunkX, regionChunkZ), chunkData);
        }
        chunkData.addPlacedBlock(new BlockPosition(x, y, z));
    }

    private @Nullable Region getRegionByCoords(String worldName, int regionX, int regionZ) {
        return regions.get(new RegionCoordinate(worldName, regionX, regionZ));
    }

    public void onChunkLoad(ServerWorld world, WorldChunk chunk) {
        if (!plugin.isRuntimeReady()) {
            return;
        }
        int chunkX = chunk.getPos().x;
        int chunkZ = chunk.getPos().z;
        int regionX = (int) Math.floor((double) chunkX / 32.0);
        int regionZ = (int) Math.floor((double) chunkZ / 32.0);
        String worldName = FabricUser.worldNameCompat(world);
        RegionCoordinate regionCoordinate = new RegionCoordinate(worldName, regionX, regionZ);
        Region region = regions.get(regionCoordinate);
        if (region == null || region.shouldReload()) {
            plugin.getScheduler().executeAsync(() -> {
                Region obtainedRegion = regions.get(regionCoordinate);
                if (obtainedRegion == null) {
                    obtainedRegion = new Region(worldName, regionX, regionZ);
                    regions.put(regionCoordinate, obtainedRegion);
                    loadRegion(obtainedRegion);
                } else if (obtainedRegion.shouldReload()) {
                    loadRegion(obtainedRegion);
                }
            });
        }
    }

    public void startAsyncSaveTimer() {
        TaskRunnable run = new TaskRunnable() {
            @Override
            public void run() {
                saveAllRegions(true, false);
            }
        };
        plugin.getScheduler().timerAsync(run, 5, 5, TimeUnit.MINUTES);
    }
}
