package dev.aurelium.auraskills.fabric.requirement;

import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.trait.FabricTraitWorlds;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/**
 * Shared harvest gate for block mixins (Paper {@code PlayerHarvestBlockEvent} parity).
 */
public final class FabricHarvestRequirement {

    private FabricHarvestRequirement() {
    }

    /**
     * @return true if harvest is allowed (or requirement system inactive / not applicable)
     */
    public static boolean allowHarvest(PlayerEntity player, BlockState harvestedState) {
        if (!(player instanceof ServerPlayerEntity sp)) {
            return true;
        }
        FabricAuraSkills plugin = FabricAuraSkills.getInstance();
        if (plugin == null || !plugin.isRuntimeReady()) {
            return true;
        }
        if (FabricTraitWorlds.isInDisabledWorld(plugin, sp)) {
            return true;
        }
        Identifier blockId = Registries.BLOCK.getId(harvestedState.getBlock());
        return FabricRequirementHandlers.checkBlock(plugin, sp, blockId, FabricRequirementHandlers.BlockContext.HARVEST);
    }
}
