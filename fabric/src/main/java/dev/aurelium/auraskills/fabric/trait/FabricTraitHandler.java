package dev.aurelium.auraskills.fabric.trait;

import dev.aurelium.auraskills.api.trait.Trait;
import dev.aurelium.auraskills.api.trait.TraitHandler;
import dev.aurelium.auraskills.common.user.User;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

/**
 * Fabric counterpart to {@code api-bukkit}'s {@code BukkitTraitHandler}.
 */
public interface FabricTraitHandler extends TraitHandler {

    double getBaseLevel(ServerPlayerEntity player, Trait trait);

    void onReload(ServerPlayerEntity player, User user, Trait trait);

    /**
     * Called when the player finishes a teleport to a different {@link ServerWorld} (dimension).
     */
    default void onPlayerChangedServerWorld(ServerPlayerEntity player, ServerWorld from, ServerWorld to) {
    }
}
