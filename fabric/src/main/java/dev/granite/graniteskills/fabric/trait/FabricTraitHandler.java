package dev.granite.graniteskills.fabric.trait;

import dev.granite.graniteskills.api.trait.Trait;
import dev.granite.graniteskills.api.trait.TraitHandler;
import dev.granite.graniteskills.common.user.User;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * Fabric counterpart to {@code api-bukkit}'s {@code BukkitTraitHandler}.
 */
public interface FabricTraitHandler extends TraitHandler {

    double getBaseLevel(ServerPlayer player, Trait trait);

    void onReload(ServerPlayer player, User user, Trait trait);

    /**
     * Called when the player finishes a teleport to a different {@link ServerLevel} (dimension).
     */
    default void onPlayerChangedServerWorld(ServerPlayer player, ServerLevel from, ServerLevel to) {
    }
}
