package dev.granite.graniteskills.fabric;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FabricXpAwardGuardTest {

    @Test
    void secondClaimForTheSameSourceAndPositionIsRejected() {
        UUID player = UUID.randomUUID();
        BlockPos pos = new BlockPos(3, 64, 9);
        assertTrue(FabricXpAwardGuard.claim(player, "graniteskills/wheat", pos));
        assertFalse(FabricXpAwardGuard.claim(player, "graniteskills/wheat", pos));
    }

    @Test
    void aDifferentDimensionOrEventStillAwards() {
        UUID player = UUID.randomUUID();
        BlockPos pos = new BlockPos(1, 2, 3);
        assertTrue(FabricXpAwardGuard.claim(player, "graniteskills/wheat", pos, "minecraft:overworld", "break"));
        assertFalse(FabricXpAwardGuard.claim(player, "graniteskills/wheat", pos, "minecraft:overworld", "break"));
        assertTrue(FabricXpAwardGuard.claim(player, "graniteskills/wheat", pos, "minecraft:the_nether", "break"));
        assertTrue(FabricXpAwardGuard.claim(player, "graniteskills/wheat", pos, "minecraft:overworld", "interact"));
    }
    @Test
    void aDifferentSourceAtTheSamePositionStillAwards() {
        UUID player = UUID.randomUUID();
        BlockPos pos = new BlockPos(8, 70, 2);
        assertTrue(FabricXpAwardGuard.claim(player, "graniteskills/wheat", pos));
        assertTrue(FabricXpAwardGuard.claim(player, "graniteskills/seeds", pos));
    }

    @Test
    void occurrenceMapStaysBoundedUnderManyDistinctPositions() {
        FabricXpAwardGuard.clearAllForTests();
        UUID player = UUID.randomUUID();
        for (int i = 0; i < 12_000; i++) {
            BlockPos pos = new BlockPos(i, 64, i % 256);
            FabricXpAwardGuard.noteBlockChanged("minecraft:overworld", pos);
            assertTrue(FabricXpAwardGuard.claim(player, "graniteskills/stone", pos, "minecraft:overworld", "break"));
        }
        FabricXpAwardGuard.pruneNow();
        assertTrue(FabricXpAwardGuard.occurrenceSize() <= 8192,
                "occurrence size=" + FabricXpAwardGuard.occurrenceSize());
        FabricXpAwardGuard.clearAllForTests();
    }
}
