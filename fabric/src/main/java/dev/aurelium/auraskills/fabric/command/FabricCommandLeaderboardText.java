package dev.aurelium.auraskills.fabric.command;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.UUID;

/**
 * Shared 1-based rank indexing and **player name** display. Online players get live names; others
 * {@code ?} in this bounded sprint (no duplicate session-service / profile plumbing).
 */
public final class FabricCommandLeaderboardText {

    /** Page size, aligned with Bukkit {@code SkillsRootCommand#onTop} and {@link
     * dev.aurelium.auraskills.common.leaderboard.LeaderboardManager#getPowerLeaderboard(int, int)}.
     */
    public static final int ENTRIES_PER_PAGE = 10;

    private FabricCommandLeaderboardText() {}

    /**
     * Global 1-based rank for an entry in a paged sublist: {@code (page-1) * pageSize + index + 1}.
     */
    public static int globalRank1Based(int page, int indexInPage, int pageSize) {
        return (Math.max(page, 1) - 1) * pageSize + indexInPage + 1;
    }

    public static int globalRank1Based(int page, int indexInPage) {
        return globalRank1Based(page, indexInPage, ENTRIES_PER_PAGE);
    }

    public static String resolvePlayerName(MinecraftServer server, UUID id) {
        ServerPlayerEntity online = server.getPlayerManager().getPlayer(id);
        if (online != null) {
            return online.getName().getString();
        }
        return "?";
    }
}
