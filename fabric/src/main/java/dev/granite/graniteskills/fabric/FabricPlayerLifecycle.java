package dev.granite.graniteskills.fabric;

import dev.granite.graniteskills.common.config.Option;
import dev.granite.graniteskills.common.storage.sql.SqlStorageProvider;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.fabric.item.FabricLegacyItemMigrator;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

public final class FabricPlayerLifecycle {

    /**
     * Chunk positions whose block entities were migrated inside {@code CHUNK_LOAD}. Entity scans
     * wait until the server tick, after the chunk-load task has finished.
     */
    private static final ConcurrentLinkedQueue<PendingChunk> PENDING_ENTITY_CHUNKS = new ConcurrentLinkedQueue<>();

    private FabricPlayerLifecycle() {
    }

    public static void register(FabricGraniteSkills plugin) {
        ServerChunkEvents.CHUNK_LOAD.register(FabricPlayerLifecycle::migrateChunk);
        ServerTickEvents.END_SERVER_TICK.register(FabricPlayerLifecycle::migrateQueuedChunkEntities);

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

    /**
     * Runs from Fabric's chunk-status task, while the server thread is blocked in
     * {@code ServerChunkCache.getChunk} waiting for that same task. {@code BlockEntity.setChanged}
     * and loot unpacking both call {@code getChunk} again and never return. Stored item stacks are
     * rewritten in place, and the chunk is marked unsaved without asking the chunk cache.
     */
    private static void migrateChunk(ServerLevel world, LevelChunk chunk, boolean generated) {
        boolean changed = false;
        for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
            if (blockEntity instanceof RandomizableContainer randomizable && randomizable.getLootTable() != null) {
                continue;
            }
            if (blockEntity instanceof Container container) {
                changed |= migrateStoredItems(container);
            }
        }
        if (changed) {
            chunk.markUnsaved();
        }
        PENDING_ENTITY_CHUNKS.add(new PendingChunk(world.dimension(), chunk.getPos().x(), chunk.getPos().z()));
    }

    private static void migrateQueuedChunkEntities(net.minecraft.server.MinecraftServer server) {
        PendingChunk pending;
        while ((pending = PENDING_ENTITY_CHUNKS.poll()) != null) {
            ServerLevel level = server.getLevel(pending.dimension());
            if (level == null || level.getChunkSource().getChunkNow(pending.x(), pending.z()) == null) {
                continue;
            }
            migrateChunkEntities(level, pending.x(), pending.z());
        }
    }

    private static void migrateChunkEntities(ServerLevel world, int chunkX, int chunkZ) {
        int minX = chunkX << 4;
        int minZ = chunkZ << 4;
        AABB box = new AABB(minX, world.getMinY(), minZ, minX + 16, world.getMaxY(), minZ + 16);
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

    private static boolean migrateStoredItems(Container container) {
        boolean changed = false;
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            if (FabricLegacyItemMigrator.migrate(stack)) {
                changed = true;
            }
        }
        return changed;
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

    private record PendingChunk(ResourceKey<Level> dimension, int x, int z) {
    }
}
