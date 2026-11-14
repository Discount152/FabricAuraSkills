package dev.granite.graniteskills.fabric.user;

import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.common.user.UserState;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import dev.granite.graniteskills.fabric.command.FabricCommandPlayerTarget;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.UserNameToIdResolver;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

/**
 * Read-only helpers to resolve a player name to a {@link UserState} using the same paths as
 * {@link FabricUserDataTransfer} / {@code /transfer} (**Sprint 59**): online {@link User#getState()}
 * or {@link dev.granite.graniteskills.common.storage.StorageProvider#loadState(java.util.UUID)}.
 */
public final class FabricUserDataRead {

    private FabricUserDataRead() {}

    /**
     * Online match (case-insensitive name) first, then {@link UserNameToIdResolver} (players the server
     * knows from prior joins). Does <strong>not</strong> scan {@code userdata/} by name.
     */
    public static Optional<UUID> resolveUuid(MinecraftServer server, String name) {
        if (name == null || name.isEmpty()) {
            return Optional.empty();
        }
        return FabricCommandPlayerTarget.findOnline(server, name)
                .map(p -> p.getUUID())
                .or(() -> offlineUuidFromCache(server, name));
    }

    private static Optional<UUID> offlineUuidFromCache(MinecraftServer server, String name) {
        UserNameToIdResolver cache = nameToIdCacheOrNull(server);
        if (cache == null) {
            return Optional.empty();
        }
        Optional<NameAndId> entry = cache.get(name);
        return entry.map(NameAndId::id);
    }

    public static @Nullable UserNameToIdResolver nameToIdCacheOrNull(MinecraftServer server) {
        if (server.services() == null) {
            return null;
        }
        return server.services().nameToIdCache();
    }

    /**
     * Loads {@link UserState} for inspection. Online: in-memory {@link User#getState()}; offline:
     * {@code loadState(uuid)} on the caller thread (same as {@link FabricTransferCommands}).
     */
    public static @Nullable UserState loadUserState(FabricGraniteSkills plugin, MinecraftServer server, UUID uuid) {
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
