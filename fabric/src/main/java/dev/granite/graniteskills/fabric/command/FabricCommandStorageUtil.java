package dev.granite.graniteskills.fabric.command;

import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.common.user.UserState;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import net.minecraft.server.level.ServerPlayer;

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
    public static void refreshOnlineUsersFromStorage(FabricGraniteSkills p) {
        for (ServerPlayer pl : p.getServer().getPlayerList().getPlayers()) {
            User u = p.getUserManager().getUser(pl.getUUID());
            if (u == null) {
                continue;
            }
            try {
                UserState st = p.getStorageProvider().loadState(pl.getUUID());
                u.applyState(st);
                p.getRewardManager().updatePermissions(u);
            } catch (Exception e) {
                p.logger()
                        .warn(
                                "Post-mutation storage refresh failed for online player "
                                        + pl.getUUID()
                                        + " — they may need to rejoin",
                                e);
            }
        }
    }
}
