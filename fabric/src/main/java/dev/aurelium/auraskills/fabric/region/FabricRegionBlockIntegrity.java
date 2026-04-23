package dev.aurelium.auraskills.fabric.region;

import dev.aurelium.auraskills.api.source.SkillSource;
import dev.aurelium.auraskills.api.source.type.BlockXpSource;
import dev.aurelium.auraskills.common.config.Option;
import dev.aurelium.auraskills.common.scheduler.Task;
import dev.aurelium.auraskills.common.scheduler.TaskRunnable;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.FabricBlockXpSourceLookup;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Bukkit {@code RegionBlockListener} subset for Fabric: pistons, falling sand/gravel,
 * tree grow same-Y cleanup, tall-plant chain removal, and delayed support checks after break.
 */
public final class FabricRegionBlockIntegrity {

    private FabricRegionBlockIntegrity() {
    }

    public static void onPistonBlocksCalculated(PistonHandler handler, World world) {
        FabricAuraSkills plugin = FabricAuraSkills.getInstance();
        if (plugin == null || !plugin.isRuntimeReady()) {
            return;
        }
        if (!plugin.configBoolean(Option.CHECK_BLOCK_REPLACE_ENABLED)) {
            return;
        }
        if (world.isClient() || !(world instanceof ServerWorld sw)) {
            return;
        }
        FabricRegionManager rm = plugin.getFabricRegionManager();
        List<BlockPos> broken = handler.getBrokenBlocks();
        for (BlockPos p : broken) {
            rm.removePlacedBlock(sw, p);
        }
        List<BlockPos> moved = handler.getMovedBlocks();
        if (moved.isEmpty()) {
            return;
        }
        Direction motion = handler.getMotionDirection();
        if (motion == null) {
            return;
        }
        Set<BlockPos> carryingPlaced = new HashSet<>();
        for (BlockPos old : moved) {
            if (rm.isPlacedBlock(sw, old)) {
                carryingPlaced.add(old);
            }
        }
        for (BlockPos old : carryingPlaced) {
            rm.removePlacedBlock(sw, old);
        }
        for (BlockPos old : carryingPlaced) {
            rm.addPlacedBlock(sw, old.offset(motion));
        }
    }

    public static void onTreePlaceBlock(ServerWorld world, BlockPos pos) {
        FabricAuraSkills plugin = FabricAuraSkills.getInstance();
        if (plugin == null || !plugin.isRuntimeReady()) {
            return;
        }
        if (!plugin.configBoolean(Option.CHECK_BLOCK_REPLACE_ENABLED)) {
            return;
        }
        Integer growY = FabricTreeGrowContext.currentGrowY();
        if (growY == null || pos.getY() != growY) {
            return;
        }
        plugin.getFabricRegionManager().removePlacedBlock(world, pos);
    }

    public static void onFallingBlockSpawned(World world, BlockPos pos, BlockState state, FallingBlockEntity entity) {
        FabricAuraSkills plugin = FabricAuraSkills.getInstance();
        if (plugin == null || !plugin.isRuntimeReady()) {
            return;
        }
        if (!plugin.configBoolean(Option.CHECK_BLOCK_REPLACE_ENABLED)) {
            return;
        }
        if (world.isClient() || !(world instanceof ServerWorld sw)) {
            return;
        }
        if (!(state.isOf(Blocks.SAND) || state.isOf(Blocks.RED_SAND) || state.isOf(Blocks.GRAVEL))) {
            return;
        }
        FabricRegionManager rm = plugin.getFabricRegionManager();
        if (!rm.isPlacedBlock(sw, pos)) {
            return;
        }
        BlockState below = sw.getBlockState(pos.down());
        if (!canFallThroughSupport(below)) {
            return;
        }
        rm.removePlacedBlock(sw, pos);
        final Block expectedBlock = state.getBlock();
        TaskRunnable poll = new TaskRunnable() {
            final AtomicInteger counter = new AtomicInteger();

            @Override
            public void run() {
                if (entity.isRemoved() || !entity.isAlive()) {
                    BlockPos landed = entity.getBlockPos();
                    BlockState current = sw.getBlockState(landed);
                    if (current.isOf(expectedBlock)) {
                        rm.addPlacedBlock(sw, landed);
                    }
                    cancel();
                    return;
                }
                BlockState cur = sw.getBlockState(entity.getBlockPos());
                String path = Registries.BLOCK.getId(cur.getBlock()).getPath();
                if (path.contains("cobweb")) {
                    cancel();
                    return;
                }
                if (counter.incrementAndGet() >= 200) {
                    cancel();
                }
            }
        };
        Task task = plugin.getScheduler().timerSync(poll, 50, 50, TimeUnit.MILLISECONDS);
        poll.injectTask(task);
    }

    private static boolean canFallThroughSupport(BlockState below) {
        if (below.isAir()) {
            return true;
        }
        if (below.isOf(Blocks.WATER)
                || below.isOf(Blocks.BUBBLE_COLUMN)
                || below.isOf(Blocks.LAVA)) {
            return true;
        }
        return false;
    }

    public static boolean isDifferentSource(FabricAuraSkills plugin, ServerWorld world, BlockPos pos, BlockXpSource expected) {
        BlockState st = world.getBlockState(pos);
        if (st.isAir()) {
            return true;
        }
        SkillSource<BlockXpSource> found = FabricBlockXpSourceLookup.findForBreak(plugin, st);
        if (found == null) {
            return true;
        }
        return !found.source().equals(expected);
    }

    public static void afterPlacedBlockRemovedOnBreak(FabricAuraSkills plugin, ServerWorld world, BlockPos brokenPos) {
        if (!plugin.configBoolean(Option.CHECK_BLOCK_REPLACE_ENABLED)) {
            return;
        }
        FabricRegionManager rm = plugin.getFabricRegionManager();
        checkTallPlantChain(world, rm, brokenPos, 0, "sugar_cane");
        checkTallPlantChain(world, rm, brokenPos, 0, "bamboo");
        checkTallPlantChain(world, rm, brokenPos, 0, "cactus");
        checkSupportBelow(plugin, world, rm, brokenPos);
        checkSupportSides(plugin, world, rm, brokenPos);
    }

    private static void checkTallPlantChain(
            ServerWorld world,
            FabricRegionManager rm,
            BlockPos from,
            int depth,
            String blockPath) {
        if (depth >= 20) {
            return;
        }
        BlockPos above = from.up();
        BlockState st = world.getBlockState(above);
        String path = Registries.BLOCK.getId(st.getBlock()).getPath();
        if (!path.equalsIgnoreCase(blockPath)) {
            return;
        }
        if (rm.isPlacedBlock(world, above)) {
            rm.removePlacedBlock(world, above);
            checkTallPlantChain(world, rm, above, depth + 1, blockPath);
        }
    }

    private static void checkSupportBelow(FabricAuraSkills plugin, ServerWorld world, FabricRegionManager rm, BlockPos broken) {
        BlockPos above = broken.up();
        SkillSource<BlockXpSource> skillSource = FabricBlockXpSourceLookup.findForBreak(plugin, world.getBlockState(above));
        BlockXpSource source = skillSource == null ? null : skillSource.source();
        if (source == null || !source.requiresSupportBlock(BlockXpSource.SupportBlockType.BELOW)) {
            return;
        }
        if (!rm.isPlacedBlock(world, above)) {
            return;
        }
        plugin.getScheduler().scheduleSync(() -> {
            if (isDifferentSource(plugin, world, above, source)) {
                rm.removePlacedBlock(world, above);
            }
        }, 50, TimeUnit.MILLISECONDS);
    }

    private static void checkSupportSides(FabricAuraSkills plugin, ServerWorld world, FabricRegionManager rm, BlockPos broken) {
        for (Direction face : Direction.values()) {
            BlockPos checked = broken.offset(face);
            SkillSource<BlockXpSource> skillSource = FabricBlockXpSourceLookup.findForBreak(plugin, world.getBlockState(checked));
            BlockXpSource source = skillSource == null ? null : skillSource.source();
            if (source == null || !source.requiresSupportBlock(BlockXpSource.SupportBlockType.SIDE)) {
                continue;
            }
            if (!rm.isPlacedBlock(world, checked)) {
                continue;
            }
            plugin.getScheduler().scheduleSync(() -> {
                if (isDifferentSource(plugin, world, checked, source)) {
                    rm.removePlacedBlock(world, checked);
                }
            }, 50, TimeUnit.MILLISECONDS);
        }
    }
}
