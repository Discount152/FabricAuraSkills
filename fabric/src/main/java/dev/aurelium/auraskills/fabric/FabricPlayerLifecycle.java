package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.common.config.Option;
import dev.aurelium.auraskills.common.storage.sql.SqlStorageProvider;
import dev.aurelium.auraskills.common.user.User;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.concurrent.TimeUnit;

public final class FabricPlayerLifecycle {

    private FabricPlayerLifecycle() {
    }

    public static void register(FabricAuraSkills plugin) {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.player;
            if (plugin.getStorageProvider() instanceof SqlStorageProvider) {
                if (plugin.configBoolean(Option.SQL_ALWAYS_LOAD_ON_JOIN) || !plugin.getUserManager().hasUser(player.getUuid())) {
                    int loadDelay = plugin.configInt(Option.SQL_LOAD_DELAY);
                    if (loadDelay == 0) {
                        loadUserAsync(plugin, player);
                    } else {
                        plugin.getScheduler().scheduleSync(() -> loadUserAsync(plugin, player), loadDelay * 50L, TimeUnit.MILLISECONDS);
                    }
                }
            } else {
                if (!plugin.getUserManager().hasUser(player.getUuid())) {
                    loadUserAsync(plugin, player);
                }
            }
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayerEntity player = handler.player;
            if (!plugin.getUserManager().hasUser(player.getUuid())) {
                return;
            }
            User user = plugin.getUserManager().getUser(player.getUuid());
            plugin.getScheduler().executeAsync(() -> {
                try {
                    user.cleanUp();
                    plugin.getStorageProvider().saveSafely(user);
                    plugin.getUserManager().removeUser(player.getUuid());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });
    }

    private static void loadUserAsync(FabricAuraSkills plugin, ServerPlayerEntity player) {
        plugin.getScheduler().executeAsync(() -> {
            try {
                plugin.getStorageProvider().load(player.getUuid(), new FabricPlayerRef(player));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
