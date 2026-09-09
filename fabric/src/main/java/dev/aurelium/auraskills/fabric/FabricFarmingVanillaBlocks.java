package dev.aurelium.auraskills.fabric;

import net.minecraft.util.Identifier;

import java.util.Set;

/**
 * Vanilla-style farming blocks aligned with bundled {@code farming.yml} plus common stem variants.
 * Used for XPDIAG probes only; XP rules still come from loaded {@link dev.aurelium.auraskills.api.source.type.BlockXpSource}.
 */
public final class FabricFarmingVanillaBlocks {

    /**
     * Paths for blocks that AuraSkills farming commonly configures (age-based, stems, stacking columns, fungi, aquatic).
     */
    private static final Set<String> MINIMUM_PATHS = Set.of(
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
            "wheat");

    private FabricFarmingVanillaBlocks() {
    }

    public static boolean isVanillaFarmCropBlock(Identifier id) {
        return id != null && "minecraft".equals(id.getNamespace()) && MINIMUM_PATHS.contains(id.getPath());
    }
}
