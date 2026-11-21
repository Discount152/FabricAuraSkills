package dev.granite.graniteskills.fabric;

import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Reads the permissions LuckPerms has already resolved for a player, including inherited grants.
 * A node whose cached value is not true is a denial and is omitted. Without LuckPerms there is no
 * permission list to walk, so the result is empty unless a probe override is installed.
 */
public final class FabricLuckPermsGrants {

    /** Deterministic probe seam: replaces the LuckPerms map for one player UUID. */
    private static final ConcurrentHashMap<UUID, List<String>> PROBE_GRANTS = new ConcurrentHashMap<>();

    private FabricLuckPermsGrants() {
    }

    /**
     * Installs a resolved grant list for probes. Treat this as LuckPerms' already-inherited,
     * denial-filtered permission map: only nodes that should contribute are present.
     */
    public static void setProbeGrants(UUID playerId, List<String> grants) {
        if (playerId == null) {
            return;
        }
        if (grants == null) {
            PROBE_GRANTS.remove(playerId);
            return;
        }
        PROBE_GRANTS.put(playerId, List.copyOf(grants));
    }

    public static void clearProbeGrants(UUID playerId) {
        if (playerId != null) {
            PROBE_GRANTS.remove(playerId);
        }
    }

    public static void clearAllProbeGrants() {
        PROBE_GRANTS.clear();
    }

    public static boolean luckPermsApiPresent() {
        try {
            Class.forName("net.luckperms.api.LuckPermsProvider");
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    public static List<String> granted(ServerPlayer player) {
        if (player == null) {
            return List.of();
        }
        List<String> probe = PROBE_GRANTS.get(player.getUUID());
        if (probe != null) {
            return probe;
        }
        try {
            Class<?> provider = Class.forName("net.luckperms.api.LuckPermsProvider");
            Object api = provider.getMethod("get").invoke(null);
            Object userManager = api.getClass().getMethod("getUserManager").invoke(api);
            Object lpUser = userManager.getClass().getMethod("getUser", UUID.class)
                    .invoke(userManager, player.getUUID());
            if (lpUser == null) {
                return List.of();
            }
            Object cached = lpUser.getClass().getMethod("getCachedData").invoke(lpUser);
            Object permissionData = cached.getClass().getMethod("getPermissionData").invoke(cached);
            Object raw = permissionData.getClass().getMethod("getPermissionMap").invoke(permissionData);
            if (!(raw instanceof Map<?, ?> permissions)) {
                return List.of();
            }
            return grantedFromPermissionMap(permissions);
        } catch (ReflectiveOperationException ignored) {
            return List.of();
        }
    }

    /**
     * LuckPerms denial contract: only {@code Boolean.TRUE} entries contribute. False/null are
     * explicit denials or unset and are omitted, matching Bukkit's {@code !permission.getValue()} skip.
     */
    public static List<String> grantedFromPermissionMap(Map<?, ?> permissions) {
        List<String> granted = new ArrayList<>();
        for (Map.Entry<?, ?> entry : permissions.entrySet()) {
            if (Boolean.TRUE.equals(entry.getValue()) && entry.getKey() instanceof String node) {
                granted.add(node);
            }
        }
        return granted;
    }
}
