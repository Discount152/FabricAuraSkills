package dev.granite.graniteskills.fabric.user;

import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import dev.granite.graniteskills.common.storage.file.FileStorageProvider;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.common.user.UserState;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.UUID;

/**
 * Shared **bounded** user-data move helpers for the Fabric {@code /transfer} command (**Sprint
 * 58**). Keeps I/O and {@link UserState} handling in one place; does not replace
 * {@link dev.granite.graniteskills.common.storage.StorageProvider} internals.
 */
public final class FabricUserDataTransfer {

    private FabricUserDataTransfer() {}

    public static boolean isPlayerOnline(MinecraftServer server, UUID uuid) {
        return server.getPlayerList().getPlayer(uuid) != null;
    }

    public static @Nullable UserState getStateForOnlineUser(FabricGraniteSkills plugin, UUID uuid) {
        User u = plugin.getUserManager().getUser(uuid);
        if (u == null) {
            return null;
        }
        return u.getState();
    }

    /**
     * For **{@link FileStorageProvider}** (YAML) only: the profile file is the strongest offline
     * “has ever played” signal without loading SQL.
     */
    public static boolean hasYamlDataFile(FabricGraniteSkills plugin, UUID uuid) {
        Path p =
                Path.of(plugin.getPluginFolder().getPath(), "userdata", uuid.toString() + ".yml");
        return java.nio.file.Files.isRegularFile(p);
    }

    public static boolean isFileStorage(FabricGraniteSkills plugin) {
        return plugin.getStorageProvider() instanceof FileStorageProvider;
    }

    /**
     * Persists a loaded {@link User} after a successful in-memory data move (Bukkit does not
     * always make this explicit; Fabric keeps it obvious for **auto-save** / restart safety).
     */
    public static void saveUserAfterStateApply(GraniteSkillsPlugin plugin, User user) {
        plugin.getStorageProvider().saveSafely(user);
        user.setShouldSave(true);
    }
}
