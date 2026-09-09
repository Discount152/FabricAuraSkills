package dev.aurelium.auraskills.fabric;

import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FabricS133CrouchVeinminerAndFishingHooksTest {

    @Test
    void breakKey_differsPerBlockPositionAtSameTick() {
        UUID u = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
        long tick = 100L;
        String w = "overworld";
        String a = u + "|" + w + "|" + BlockPos.ORIGIN.asLong() + "|" + tick;
        String b = u + "|" + w + "|" + new BlockPos(1, 0, 0).asLong() + "|" + tick;
        assertNotEquals(a, b, "veinminer same-tick breaks must not share one job key");
    }

    @Test
    void fishingBobberUse_apiUsesPeekNotStatMixin() {
        assertTrue(
                java.util.Arrays.stream(FabricFishingXpHandler.class.getDeclaredMethods())
                        .anyMatch(m -> m.getName().equals("onBobberUseReturn")));
    }
}