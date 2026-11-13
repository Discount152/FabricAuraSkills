package dev.granite.graniteskills.fabric;

import dev.granite.graniteskills.common.config.Option;
import dev.granite.graniteskills.common.storage.sql.SqlStorageProvider;
import dev.granite.graniteskills.common.user.User;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;

import java.util.concurrent.TimeUnit;

public final class FabricPlayerLifecycle {

    private FabricPlayerLifecycle() {
    }

    public static void register(FabricGraniteSkills plugin) {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.player;
            if (plugin.getStorageProvider() instanceof SqlStorageProvider) {
                if (plugin.configBoolean(Option.SQL_ALWAYS_LOAD_ON_JOIN) || !plugin.getUserManager().hasUser(player.getUUID())) {
                    int loadDelay = plugin.configInt(Option.SQL_LOAD_DELAY);
                    if (loadDelay == 0) {
                        loadUserAsync(plugin, player);
                    } else {
                        plugin.getScheduler().scheduleSync(() -> loadUserAsync(plugin, player), loadDelay * 50L, TimeUnit.MILLISECONDS);
                    }
                }
            } else {
                if (!plugin.getUserManager().hasUser(player.getUUID())) {
                    loadUserAsync(plugin, player);
                }
            }
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayer player = handler.player;
            if (!plugin.getUserManager().hasUser(player.getUUID())) {
                return;
            }
            User user = plugin.getUserManager().getUser(player.getUUID());
            plugin.getScheduler().executeAsync(() -> {
                try {
                    user.cleanUp();
                    plugin.getStorageProvider().saveSafely(user);
                    plugin.getUserManager().removeUser(player.getUUID());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });
    }

    private static void loadUserAsync(FabricGraniteSkills plugin, ServerPlayer player) {
        plugin.getScheduler().executeAsync(() -> {
            try {
                plugin.getStorageProvider().load(player.getUUID(), new FabricPlayerRef(player));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
