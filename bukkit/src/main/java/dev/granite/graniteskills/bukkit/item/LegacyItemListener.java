package dev.granite.graniteskills.bukkit.item;

import dev.granite.graniteskills.bukkit.GraniteSkills;
import org.bukkit.block.BlockState;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

/**
 * Rewrites items still stored under the AuraSkills and AureliumSkills names when they are loaded.
 */
public class LegacyItemListener implements Listener {

    private final GraniteSkills plugin;

    public LegacyItemListener(GraniteSkills plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (plugin.isNbtApiDisabled()) {
            return;
        }
        Player player = event.getPlayer();
        migrateInventory(player.getInventory());
        migrateInventory(player.getEnderChest());
        ItemStack cursor = player.getItemOnCursor();
        if (BukkitLegacyItemMigrator.migrate(cursor)) {
            player.setItemOnCursor(cursor);
        }
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        if (plugin.isNbtApiDisabled()) {
            return;
        }
        for (BlockState state : event.getChunk().getTileEntities()) {
            if (state instanceof InventoryHolder holder) {
                migrateInventory(holder.getInventory());
            }
        }
        for (Entity entity : event.getChunk().getEntities()) {
            migrateEntity(entity);
        }
    }

    @EventHandler
    public void onOpen(InventoryOpenEvent event) {
        if (plugin.isNbtApiDisabled() || event.getInventory().getHolder() instanceof Player) {
            return;
        }
        migrateInventory(event.getInventory());
    }

    private static void migrateInventory(Inventory inventory) {
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (BukkitLegacyItemMigrator.migrate(stack)) {
                inventory.setItem(slot, stack);
            }
        }
    }

    private static void migrateEntity(Entity entity) {
        if (entity instanceof Item item) {
            ItemStack stack = item.getItemStack();
            if (BukkitLegacyItemMigrator.migrate(stack)) {
                item.setItemStack(stack);
            }
            return;
        }
        if (entity instanceof ItemFrame frame) {
            ItemStack stack = frame.getItem();
            if (BukkitLegacyItemMigrator.migrate(stack)) {
                frame.setItem(stack);
            }
            return;
        }
        if (entity instanceof ArmorStand stand) {
            EntityEquipment equipment = stand.getEquipment();
            if (equipment == null) {
                return;
            }
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                ItemStack stack = equipment.getItem(slot);
                if (BukkitLegacyItemMigrator.migrate(stack)) {
                    equipment.setItem(slot, stack);
                }
            }
        }
    }

}
