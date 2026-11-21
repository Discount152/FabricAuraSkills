package dev.granite.graniteskills.fabric.region;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlacedBlockMovesTest {

    @Test
    void moveTransfersATrackedPositionAndLeavesUntrackedBlocksAlone() {
        Set<String> positions = new HashSet<>();
        positions.add("minecraft:overworld 1 2 3");
        assertTrue(PlacedBlockMoves.move(positions, "minecraft:overworld 1 2 3", "minecraft:overworld 2 2 3"));
        assertTrue(positions.contains("minecraft:overworld 2 2 3"));
        assertFalse(positions.contains("minecraft:overworld 1 2 3"));
        assertFalse(PlacedBlockMoves.move(positions, "minecraft:overworld 9 9 9", "minecraft:overworld 8 9 9"));
    }
}
