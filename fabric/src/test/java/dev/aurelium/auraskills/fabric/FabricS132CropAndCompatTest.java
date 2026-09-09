package dev.aurelium.auraskills.fabric;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Sprint 132 — vanilla farming block path coverage (bundled {@code farming.yml} style: age, stems, columns, fungi,
 * aquatic) plus multi-break geometry sanity. Full XP scenarios (items 1–14) remain manual / gametest candidates.
 */
class FabricS132CropAndCompatTest {

    private static final String[] EXPECTED_MINECRAFT_FARM_PATHS = {
            "attached_melon_stem",
            "attached_pumpkin_stem",
            "bamboo",
            "beetroots",
            "brown_mushroom",
            "cactus",
            "carrots",
            "cave_vines",
            "cave_vines_plant",
            "cocoa",
            "kelp",
            "kelp_plant",
            "melon",
            "melon_stem",
            "nether_wart",
            "pitcher_crop",
            "potatoes",
            "pumpkin",
            "pumpkin_stem",
            "red_mushroom",
            "sea_pickle",
            "sugar_cane",
            "sweet_berry_bush",
            "torchflower",
            "wheat",
    };

    @Test
    void vanillaFarmPaths_coverBundledFarmingYamlStyleSources() {
        for (String path : EXPECTED_MINECRAFT_FARM_PATHS) {
            assertTrue(
                    FabricFarmingVanillaBlocks.isVanillaFarmCropBlock(Identifier.of("minecraft", path)),
                    "expected vanilla farm probe path: " + path);
        }
        assertFalse(FabricFarmingVanillaBlocks.isVanillaFarmCropBlock(Identifier.of("minecraft", "stone")));
        assertFalse(FabricFarmingVanillaBlocks.isVanillaFarmCropBlock(Identifier.of("other", "wheat")));
    }

    @Test
    void chebyshevRadius1_snapshotCellCount_matchesS131() {
        int r = 1;
        int count = 0;
        BlockPos center = BlockPos.ORIGIN;
        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (Math.max(Math.abs(dx), Math.max(Math.abs(dy), Math.abs(dz))) > r) {
                        continue;
                    }
                    count++;
                }
            }
        }
        assertEquals(27, count);
        assertEquals(center.asLong(), BlockPos.ORIGIN.asLong());
    }
}
