package dev.aurelium.auraskills.fabric.command;

import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.common.user.UserState;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Shared storage-side helpers for admin commands (backup load, storage mutations, …).
 * **Sprint 64.**
 */
public final class FabricCommandStorageUtil {

    private FabricCommandStorageUtil() {}

    /**
     * After {@code applyState} to underlying storage, reload {@link UserState} for every online
     * player from storage so in-memory data matches.
     */
    public static void refreshOnlineUsersFromStorage(FabricAuraSkills p) {
        for (ServerPlayerEntity pl : p.getServer().getPlayerManager().getPlayerList()) {
            User u = p.getUserManager().getUser(pl.getUuid());
            if (u == null) {
                continue;
            }
            try {
                UserState st = p.getStorageProvider().loadState(pl.getUuid());
                u.applyState(st);
                p.getRewardManager().updatePermissions(u);
            } catch (Exception e) {
                p.logger()
                        .warn(
                                "Post-mutation storage refresh failed for online player "
                                        + pl.getUuid()
                                        + " — they may need to rejoin",
                                e);
            }
        }
    }
}
