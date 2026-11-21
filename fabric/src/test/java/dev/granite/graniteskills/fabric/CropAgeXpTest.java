package dev.granite.graniteskills.fabric;

import com.google.common.collect.ImmutableMap;
import dev.granite.graniteskills.api.registry.NamespacedId;
import dev.granite.graniteskills.api.source.SourceValues;
import dev.granite.graniteskills.api.source.type.BlockXpSource;
import dev.granite.graniteskills.common.source.type.BlockSource;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CropAgeXpTest {

    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void matureCropAwardsXpAndANotReadyCropAwardsNothing() {
        BlockXpSource wheat = crop("wheat", 3.0, new int[]{7}, "");
        assertEquals(0.0, FabricBlockStateMatcher.awardedBreakXp(wheat(0), wheat));
        assertEquals(0.0, FabricBlockStateMatcher.awardedBreakXp(wheat(6), wheat));
        assertEquals(3.0, FabricBlockStateMatcher.awardedBreakXp(wheat(7), wheat));

        BlockXpSource berries = crop("sweet_berry_bush", 2.5, new int[]{2, 3}, "age-1");
        assertEquals(0.0, FabricBlockStateMatcher.awardedBreakXp(berry(1), berries));
        assertEquals(2.5, FabricBlockStateMatcher.awardedBreakXp(berry(2), berries));
        assertEquals(5.0, FabricBlockStateMatcher.awardedBreakXp(berry(3), berries));
    }

    private static BlockState wheat(int age) {
        return Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE, age);
    }

    private static BlockState berry(int age) {
        return Blocks.SWEET_BERRY_BUSH.defaultBlockState().setValue(SweetBerryBushBlock.AGE, age);
    }

    private static BlockXpSource crop(String block, double xp, int[] ages, String multiplier) {
        BlockXpSource.BlockXpSourceState[] states = new BlockXpSource.BlockXpSourceState[ages.length];
        for (int i = 0; i < ages.length; i++) {
            states[i] = new BlockSource.BlockSourceState(ImmutableMap.of("age", ages[i]));
        }
        return new BlockSource(
                null,
                new SourceValues(null, null, NamespacedId.fromDefault(block), xp, null, null, null),
                new String[]{block},
                1,
                new BlockXpSource.BlockTriggers[]{BlockXpSource.BlockTriggers.BREAK},
                false,
                states,
                null,
                multiplier,
                BlockXpSource.SupportBlockType.NONE,
                false);
    }
}
