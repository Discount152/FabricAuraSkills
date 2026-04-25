package dev.aurelium.auraskills.fabric.user;

import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.common.user.UserState;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.command.FabricCommandPlayerTarget;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.util.NameToIdCache;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

/**
 * Read-only helpers to resolve a player name to a {@link UserState} using the same paths as
 * {@link FabricUserDataTransfer} / {@code /transfer} (**Sprint 59**): online {@link User#getState()}
 * or {@link dev.aurelium.auraskills.common.storage.StorageProvider#loadState(java.util.UUID)}.
 */
public final class FabricUserDataRead {

    private FabricUserDataRead() {}

    /**
     * Online match (case-insensitive name) first, then {@link NameToIdCache} (players the server
     * knows from prior joins). Does <strong>not</strong> scan {@code userdata/} by name.
     */
    public static Optional<UUID> resolveUuid(MinecraftServer server, String name) {
        if (name == null || name.isEmpty()) {
            return Optional.empty();
        }
        return FabricCommandPlayerTarget.findOnline(server, name)
                .map(p -> p.getUuid())
                .or(() -> offlineUuidFromCache(server, name));
    }

    private static Optional<UUID> offlineUuidFromCache(MinecraftServer server, String name) {
        NameToIdCache cache = nameToIdCacheOrNull(server);
        if (cache == null) {
            return Optional.empty();
        }
        Optional<PlayerConfigEntry> entry = cache.findByName(name);
        return entry.map(PlayerConfigEntry::id);
    }

    public static @Nullable NameToIdCache nameToIdCacheOrNull(MinecraftServer server) {
        if (server.getApiServices() == null) {
            return null;
        }
        return server.getApiServices().nameToIdCache();
    }

    /**
     * Loads {@link UserState} for inspection. Online: in-memory {@link User#getState()}; offline:
     * {@code loadState(uuid)} on the caller thread (same as {@link FabricTransferCommands}).
     */
    public static @Nullable UserState loadUserState(FabricAuraSkills plugin, MinecraftServer server, UUID uuid) {
        if (FabricUserDataTransfer.isPlayerOnline(server, uuid)) {
            return FabricUserDataTransfer.getStateForOnlineUser(plugin, uuid);
        }
        try {
            return plugin.getStorageProvider().loadState(uuid);
        } catch (Exception e) {
            return null;
        }
    }
}
