package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.api.source.SkillSource;
import dev.aurelium.auraskills.api.source.type.BlockXpSource;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;

import java.util.List;

/**
 * Resolves the {@link BlockXpSource} (BREAK trigger) for a block state, matching
 * Bukkit {@code BlockLeveler#getSource(block, BREAK)} and {@link FabricBlockBreakHandler} logic.
 */
public final class FabricBlockXpSourceLookup {

    private FabricBlockXpSourceLookup() {
    }

    public static SkillSource<BlockXpSource> findForBreak(FabricAuraSkills plugin, BlockState state) {
        if (plugin == null || !plugin.isRuntimeReady()) {
            return null;
        }
        String path = Registries.BLOCK.getId(state.getBlock()).getPath();
        for (SkillSource<BlockXpSource> entry : plugin.getSkillManager().getSourcesOfType(BlockXpSource.class)) {
            BlockXpSource source = entry.source();
            if (!List.of(source.getTriggers()).contains(BlockXpSource.BlockTriggers.BREAK)) {
                continue;
            }
            boolean blockOk = false;
            for (String blockName : source.getBlocks()) {
                if (blockName != null && blockName.equalsIgnoreCase(path)) {
                    blockOk = true;
                    break;
                }
            }
            if (!blockOk) {
                continue;
            }
            BlockXpSource.BlockXpSourceState[] states = source.getStates();
            if (states != null && states.length > 0) {
                if (!FabricBlockStateMatcher.matchesStates(state, states)) {
                    continue;
                }
            }
            return entry;
        }
        return null;
    }
}
