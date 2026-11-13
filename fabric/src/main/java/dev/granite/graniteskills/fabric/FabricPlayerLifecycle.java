package dev.granite.graniteskills.fabric;

import dev.granite.graniteskills.common.config.Option;
import dev.granite.graniteskills.common.storage.sql.SqlStorageProvider;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.fabric.item.FabricLegacyItemMigrator;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;

import java.util.concurrent.TimeUnit;

public final class FabricPlayerLifecycle {

    private FabricPlayerLifecycle() {
    }

    public static void register(FabricGraniteSkills plugin) {
        ServerChunkEvents.CHUNK_LOAD.register(FabricPlayerLifecycle::migrateChunk);

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.player;
            migratePlayer(player);
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

    private static void migratePlayer(ServerPlayer player) {
        migrateContainer(player.getInventory());
        migrateContainer(player.getEnderChestInventory());
        ItemStack carried = player.containerMenu.getCarried();
        if (FabricLegacyItemMigrator.migrate(carried)) {
            player.containerMenu.setCarried(carried);
        }
    }

    private static void migrateChunk(ServerLevel world, LevelChunk chunk, boolean generated) {
        for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
            if (blockEntity instanceof Container container) {
                migrateContainer(container);
                blockEntity.setChanged();
            }
        }
        var pos = chunk.getPos();
        AABB box = new AABB(pos.getMinBlockX(), world.getMinY(), pos.getMinBlockZ(), pos.getMinBlockX() + 16, world.getMaxY(), pos.getMinBlockZ() + 16);
        for (ItemEntity entity : world.getEntitiesOfClass(ItemEntity.class, box)) {
            ItemStack stack = entity.getItem();
            if (FabricLegacyItemMigrator.migrate(stack)) {
                entity.setItem(stack);
            }
        }
        for (ItemFrame frame : world.getEntitiesOfClass(ItemFrame.class, box)) {
            ItemStack stack = frame.getItem();
            if (FabricLegacyItemMigrator.migrate(stack)) {
                frame.setItem(stack);
            }
        }
        for (ArmorStand stand : world.getEntitiesOfClass(ArmorStand.class, box)) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                ItemStack stack = stand.getItemBySlot(slot);
                if (FabricLegacyItemMigrator.migrate(stack)) {
                    stand.setItemSlot(slot, stack);
                }
            }
        }
    }

    private static void migrateContainer(Container container) {
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            if (FabricLegacyItemMigrator.migrate(stack)) {
                container.setItem(slot, stack);
            }
        }
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
