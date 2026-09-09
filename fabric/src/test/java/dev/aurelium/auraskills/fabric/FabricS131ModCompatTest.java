package dev.aurelium.auraskills.fabric;

import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Sprint 131 — classpath config + geometry checks (no static Minecraft item/block registry).
 */
class FabricS131ModCompatTest {

    @Test
    void embeddedConfig_containsModCompatKeys() throws Exception {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("config.yml")) {
            assertNotNull(in, "config.yml should be bundled");
            String text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(text.contains("mod_compat:"));
            assertTrue(text.contains("multi_break_xp:"));
            assertTrue(text.contains("interaction_harvest_xp:"));
            assertTrue(text.contains("tag_fallback_for_fish:"));
        }
    }

    @Test
    void chebyshevRadius1_has27Cells() {
        Map<Long, BlockPos> seen = new HashMap<>();
        BlockPos center = BlockPos.ORIGIN;
        int r = 1;
        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (Math.max(Math.abs(dx), Math.max(Math.abs(dy), Math.abs(dz))) > r) {
                        continue;
                    }
                    BlockPos p = center.add(dx, dy, dz);
                    seen.put(p.asLong(), p);
                }
            }
        }
        assertEquals(27, seen.size());
        assertTrue(seen.containsKey(center.asLong()));
    }
}
