package dev.aurelium.auraskills.fabric;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Tracks per-world brewing stand "owner" UUID for {@link dev.aurelium.auraskills.api.source.type.BrewingXpSource.BrewTriggers#BREW}
 * XP (Bukkit {@code skillsBrewingStandOwner} metadata). Claim on first GUI open; set on player placement.
 */
public final class FabricBrewingOwnerTracker {

    private static final ConcurrentHashMap<FabricBrewingTakeoutTracker.BrewStandKey, UUID> OWNERS =
            new ConcurrentHashMap<>();
    private static final AtomicBoolean REGISTERED = new AtomicBoolean(false);

    private FabricBrewingOwnerTracker() {
    }

    public static void register() {
        if (!REGISTERED.compareAndSet(false, true)) {
            return;
        }
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (world.isClientSide() || !(world instanceof ServerLevel sw)) {
                return;
            }
            if (!state.is(Blocks.BREWING_STAND)) {
                return;
            }
            remove(sw, pos);
        });
    }

    public static void setOwner(ServerLevel world, BlockPos pos, UUID owner) {
        OWNERS.put(key(world, pos), owner);
    }

    /**
     * Bukkit {@code InventoryOpenEvent}: first player to open an unclaimed stand becomes owner.
     */
    public static void claimIfAbsent(ServerLevel world, BlockPos pos, UUID playerId) {
        OWNERS.putIfAbsent(key(world, pos), playerId);
    }

    public static @Nullable UUID getOwner(ServerLevel world, BlockPos pos) {
        return OWNERS.get(key(world, pos));
    }

    public static void remove(ServerLevel world, BlockPos pos) {
        OWNERS.remove(key(world, pos));
    }

    private static FabricBrewingTakeoutTracker.BrewStandKey key(ServerLevel world, BlockPos pos) {
        return new FabricBrewingTakeoutTracker.BrewStandKey(world.dimension(), pos);
    }
}
