package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.common.user.User;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

public final class FabricXpUserResolution {

    private FabricXpUserResolution() {
    }

    public static @Nullable User resolve(FabricAuraSkills plugin, ServerPlayerEntity player) {
        if (plugin == null || player == null) {
            return null;
        }
        if (plugin.getUserManager() instanceof FabricUserManager fum) {
            return fum.getUser(player);
        }
        return plugin.getUserManager().getUser(player.getUuid());
    }
}
