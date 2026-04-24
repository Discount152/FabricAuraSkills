package dev.aurelium.auraskills.fabric.command;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.NameToIdCache;

import java.util.Optional;
import java.util.UUID;

/**
 * Shared 1-based rank indexing and **player name** display. Online players use live names; offline
 * entries use the server API services {@link NameToIdCache} when a name is known (typical for
 * players who have joined before). If there is no cache entry, {@code ?} — we do not store
 * usernames in {@code UserState}.
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
        NameToIdCache cache = server.getApiServices().nameToIdCache();
        if (cache == null) {
            return "?";
        }
        Optional<PlayerConfigEntry> entry = cache.getByUuid(id);
        return entry.map(PlayerConfigEntry::name).orElse("?");
    }
}
