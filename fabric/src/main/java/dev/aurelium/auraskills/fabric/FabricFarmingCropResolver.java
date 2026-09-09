package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.api.source.SkillSource;
import dev.aurelium.auraskills.api.source.type.BlockXpSource;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;

import java.util.List;

/**
 * State-aware farming helpers: resolves BREAK sources from a captured {@link BlockState} and classifies maturity
 * against configured {@link BlockXpSource#getStates()} (AuraSkills / Paper-aligned behavior, not Bukkit APIs).
 */
public final class FabricFarmingCropResolver {

    private FabricFarmingCropResolver() {
    }

    public static SkillSource<BlockXpSource> findBreakMatch(FabricAuraSkills plugin, BlockState state) {
        return FabricBlockXpSourceLookup.findForBreak(plugin, state);
    }

    /**
     * {@code true} if any enabled BREAK {@link BlockXpSource} for this block id has {@code states} matching
     * {@code state} (including sources with no state filter).
     */
    public static boolean isMatureOrMatchingFarmBreakState(FabricAuraSkills plugin, BlockState state) {
        if (plugin == null || !plugin.isRuntimeReady()) {
            return false;
        }
        for (SkillSource<BlockXpSource> entry : plugin.getSkillManager().getSourcesOfType(BlockXpSource.class)) {
            BlockXpSource source = entry.source();
            if (!List.of(source.getTriggers()).contains(BlockXpSource.BlockTriggers.BREAK)) {
                continue;
            }
            boolean blockOk = false;
            for (String blockName : source.getBlocks()) {
                if (FabricBlockXpSourceLookup.breakSourceBlockMatchesState(state, blockName, entry.skill())) {
                    blockOk = true;
                    break;
                }
            }
            if (!blockOk) {
                continue;
            }
            BlockXpSource.BlockXpSourceState[] states = source.getStates();
            if (states == null || states.length == 0) {
                return true;
            }
            if (FabricBlockStateMatcher.matchesStates(state, states)) {
                return true;
            }
        }
        return false;
    }

    public static String breakLookupKey(BlockState state) {
        return "break:block:" + Registries.BLOCK.getId(state.getBlock());
    }
}
