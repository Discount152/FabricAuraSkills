package dev.aurelium.auraskills.fabric.requirement;

import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.trait.FabricTraitWorlds;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Shared harvest gate for block mixins (Paper {@code PlayerHarvestBlockEvent} parity).
 */
public final class FabricHarvestRequirement {

    private FabricHarvestRequirement() {
    }

    /**
     * @return true if harvest is allowed (or requirement system inactive / not applicable)
     */
    public static boolean allowHarvest(Player player, BlockState harvestedState) {
        if (!(player instanceof ServerPlayer sp)) {
            return true;
        }
        FabricAuraSkills plugin = FabricAuraSkills.getInstance();
        if (plugin == null || !plugin.isRuntimeReady()) {
            return true;
        }
        if (FabricTraitWorlds.isInDisabledWorld(plugin, sp)) {
            return true;
        }
        Identifier blockId = BuiltInRegistries.BLOCK.getKey(harvestedState.getBlock());
        return FabricRequirementHandlers.checkBlock(plugin, sp, blockId, FabricRequirementHandlers.BlockContext.HARVEST);
    }
}
