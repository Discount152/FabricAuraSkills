package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.api.source.type.BlockXpSource;
import dev.aurelium.auraskills.common.config.Option;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Mirrors Bukkit {@link dev.aurelium.auraskills.bukkit.source.BlockLevelerHelper#getBlocksBroken} for interact (and
 * other) XP. Fabric has no placed-block API yet; {@code isPlaced} is always treated as {@code false} (same as
 * {@link FabricTreecapitator#isPlayerPlacedBlock}).
 */
public final class FabricBlockLevelerHelper {

    private static final int MAX_TALL_SCAN = 384;

    private FabricBlockLevelerHelper() {
    }

    public static int getBlocksBroken(ServerLevel world, BlockPos pos, BlockXpSource source, FabricAuraSkills plugin) {
        BlockState state = world.getBlockState(pos);
        String path = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
        boolean checkReplace = plugin.configBoolean(Option.CHECK_BLOCK_REPLACE_ENABLED);

        if ("sugar_cane".equals(path)) {
            return sugarCaneColumn(world, pos, checkReplace);
        }
        if ("bamboo".equals(path) || "cactus".equals(path) || "kelp".equals(path) || "kelp_plant".equals(path)) {
            return tallPlantColumn(world, pos, source, checkReplace);
        }
        return 1;
    }

    private static int sugarCaneColumn(ServerLevel world, BlockPos pos, boolean checkReplace) {
        int num = 1;
        BlockPos.MutableBlockPos m = pos.mutable().move(Direction.UP);
        if (!isSugarCane(world, m)) {
            return num;
        }
        if (!isPlaced(world, m) || !checkReplace) {
            num++;
        }
        m.move(Direction.UP);
        if (isSugarCane(world, m) && (!isPlaced(world, m) || !checkReplace)) {
            num++;
        }
        return num;
    }

    private static boolean isSugarCane(ServerLevel world, BlockPos pos) {
        return "sugar_cane".equals(BuiltInRegistries.BLOCK.getKey(world.getBlockState(pos).getBlock()).getPath());
    }

    private static int tallPlantColumn(ServerLevel world, BlockPos pos, BlockXpSource source, boolean checkReplace) {
        int num = 1;
        if (checkReplace && isPlaced(world, pos)) {
            BlockPos up = pos.above();
            if (!blockMatchesSource(world, up, source) || isPlaced(world, up)) {
                return 0;
            }
            num = 0;
        }
        return num + countMatchingColumnAbove(world, pos.above(), source, checkReplace);
    }

    private static int countMatchingColumnAbove(ServerLevel world, BlockPos start, BlockXpSource source,
            boolean checkReplace) {
        BlockPos.MutableBlockPos m = start.mutable();
        int num = 0;
        for (int i = 0; i < MAX_TALL_SCAN; i++) {
            if (!blockMatchesSource(world, m, source)) {
                break;
            }
            if (checkReplace && isPlaced(world, m)) {
                break;
            }
            num++;
            m.move(Direction.UP);
        }
        return num;
    }

    private static boolean blockMatchesSource(ServerLevel world, BlockPos pos, BlockXpSource source) {
        String path = BuiltInRegistries.BLOCK.getKey(world.getBlockState(pos).getBlock()).getPath();
        for (String blockName : source.getBlocks()) {
            if (blockName != null && blockName.equalsIgnoreCase(path)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isPlaced(ServerLevel world, BlockPos pos) {
        return false;
    }
}
