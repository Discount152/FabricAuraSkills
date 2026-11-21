package dev.granite.graniteskills.fabric;

import com.google.common.collect.ImmutableMap;
import dev.granite.graniteskills.api.registry.NamespacedId;
import dev.granite.graniteskills.api.source.SourceValues;
import dev.granite.graniteskills.api.source.type.BlockXpSource;
import dev.granite.graniteskills.common.source.type.BlockSource;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HarvestXpTest {

    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void rightClickHarvestAwardsOnceAndARepeatedActionAwardsNothing() {
        BlockXpSource berries = berries();
        BlockState ready = Blocks.SWEET_BERRY_BUSH.defaultBlockState().setValue(SweetBerryBushBlock.AGE, 3);
        BlockState harvested = Blocks.SWEET_BERRY_BUSH.defaultBlockState().setValue(SweetBerryBushBlock.AGE, 1);
        BlockState untouched = Blocks.SWEET_BERRY_BUSH.defaultBlockState().setValue(SweetBerryBushBlock.AGE, 3);
        double snapshot = FabricBlockStateMatcher.awardedBreakXp(ready, berries);
        assertEquals(5.0, snapshot);

        UUID player = UUID.randomUUID();
        BlockPos pos = new BlockPos(4, 70, 4);
        String sourceId = berries.getId().toString();
        boolean first = FabricXpAwardGuard.claim(player, sourceId, pos);
        boolean second = FabricXpAwardGuard.claim(player, sourceId, pos);

        assertEquals(5.0, FabricBlockStateMatcher.harvestAward(snapshot, harvested, berries.getAfterStates(), first));
        assertEquals(0.0, FabricBlockStateMatcher.harvestAward(snapshot, harvested, berries.getAfterStates(), second));
        assertEquals(0.0, FabricBlockStateMatcher.harvestAward(snapshot, untouched, berries.getAfterStates(), true));
        assertEquals(0.0, FabricBlockStateMatcher.awardedBreakXp(
                Blocks.SWEET_BERRY_BUSH.defaultBlockState().setValue(SweetBerryBushBlock.AGE, 1), berries));
    }

    private static BlockXpSource berries() {
        BlockXpSource.BlockXpSourceState[] states = new BlockXpSource.BlockXpSourceState[]{
                new BlockSource.BlockSourceState(ImmutableMap.of("age", 2)),
                new BlockSource.BlockSourceState(ImmutableMap.of("age", 3))
        };
        BlockXpSource.BlockXpSourceState[] after = new BlockXpSource.BlockXpSourceState[]{
                new BlockSource.BlockSourceState(ImmutableMap.of("age", 1))
        };
        return new BlockSource(
                null,
                new SourceValues(null, null, NamespacedId.fromDefault("sweet_berry_bush"), 2.5, null, null, null),
                new String[]{"sweet_berry_bush"},
                1,
                new BlockXpSource.BlockTriggers[]{BlockXpSource.BlockTriggers.BREAK, BlockXpSource.BlockTriggers.INTERACT},
                false,
                states,
                after,
                "age-1",
                BlockXpSource.SupportBlockType.NONE,
                false);
    }
}
