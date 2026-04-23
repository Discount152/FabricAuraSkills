package dev.aurelium.auraskills.fabric.region;

import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

/**
 * Bukkit {@code RegionBlockListener#checkBreak} — remove placed flags when the block is broken (after XP / loot
 * {@link dev.aurelium.auraskills.fabric.FabricBlockBreakHandler} / {@code FabricBlockLootHandler} run, since those
 * register earlier as {@code AFTER}).
 * Tall-plant / support-tick follow-up: {@link FabricRegionBlockIntegrity#afterPlacedBlockRemovedOnBreak}.
 */
public final class FabricPlacedBlockBreakHandler {

    private FabricPlacedBlockBreakHandler() {
    }

    public static void register(FabricAuraSkills plugin) {
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (world.isClient() || !(player instanceof ServerPlayerEntity) || !(world instanceof ServerWorld sw)) {
                return;
            }
            if (plugin == null || !plugin.isRuntimeReady()) {
                return;
            }
            plugin.getFabricRegionManager().removePlacedBlock(sw, pos);
            FabricRegionBlockIntegrity.afterPlacedBlockRemovedOnBreak(plugin, sw, pos);
        });
    }
}
