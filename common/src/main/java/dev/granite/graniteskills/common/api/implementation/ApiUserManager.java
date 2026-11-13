package dev.granite.graniteskills.common.api.implementation;

import dev.granite.graniteskills.api.user.SkillsUser;
import dev.granite.graniteskills.api.user.UserManager;
import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import dev.granite.graniteskills.common.user.User;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ApiUserManager implements UserManager {

    private final GraniteSkillsPlugin plugin;

    public ApiUserManager(GraniteSkillsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public SkillsUser getUser(UUID playerId) {
        User user = plugin.getUserManager().getUser(playerId);
        if (user != null) {
            return new ApiSkillsUser(user);
        }
        return new OfflineSkillsUser(plugin, playerId);
    }

    @Override
    public CompletableFuture<SkillsUser> loadUser(UUID playerId) {
        CompletableFuture<SkillsUser> future = new CompletableFuture<>();
        User onlineUser = plugin.getUserManager().getUser(playerId);
        if (onlineUser != null) {
            future.complete(new ApiSkillsUser(onlineUser));
        } else {
            plugin.getScheduler().executeAsync(() -> {
                try {
                    plugin.getStorageProvider().load(playerId, null);
                    User loadedUser = plugin.getUserManager().getUser(playerId);
                    if (loadedUser != null) {
                        future.complete(new ApiSkillsUser(loadedUser));
                    } else {
                        future.complete(new OfflineSkillsUser(plugin, playerId));
                    }
                } catch (Exception e) {
                    future.complete(new OfflineSkillsUser(plugin, playerId));
                    e.printStackTrace();
                }
            });
        }
        return future;
    }

}
