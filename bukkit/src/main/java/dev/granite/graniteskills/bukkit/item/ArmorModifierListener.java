package dev.granite.graniteskills.bukkit.item;

import dev.granite.graniteskills.api.stat.ReloadableIdentifier;
import dev.granite.graniteskills.bukkit.GraniteSkills;
import dev.granite.graniteskills.bukkit.util.armor.ArmorEquipEvent;
import dev.granite.graniteskills.bukkit.util.armor.ArmorType;
import dev.granite.graniteskills.common.config.Option;
import dev.granite.graniteskills.common.scheduler.TaskRunnable;
import dev.granite.graniteskills.common.user.User;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class ArmorModifierListener implements Listener {

    private final GraniteSkills plugin;
    private final ItemStateManager stateManager;

    public ArmorModifierListener(GraniteSkills plugin) {
        this.plugin = plugin;
        this.stateManager = new ItemStateManager(plugin);
        if (plugin.configBoolean(Option.MODIFIER_ARMOR_TIMER_ENABLED)) {
            startTimer();
        }
    }

    @EventHandler
    public void onEquip(ArmorEquipEvent event) {
        if (event.isCancelled()) return;
        if (plugin.configBoolean(Option.MODIFIER_ARMOR_TIMER_ENABLED)) return; // Don't use if timer is enabled
        Player player = event.getPlayer();
        User user = plugin.getUser(player);

        ItemStack newPiece = event.getNewArmorPiece();
        if (newPiece == null) {
            newPiece = new ItemStack(Material.AIR);
        }

        ArmorType type = event.getType();
        if (type != null) {
            stateManager.changeItemInSlot(user, player, newPiece, type.getEquipmentSlot());
        }
    }

    // Timer based detection
    private void startTimer() {
        var task = new TaskRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    User user = plugin.getUser(player);
                    Set<ReloadableIdentifier> toReload = new HashSet<>();

                    for (ArmorType armorType : ArmorType.values()) { // Go through each armor slot
                        ItemStack wearing = player.getInventory().getItem(armorType.getEquipmentSlot()); // Get the armor player is currently wearing
                        if (wearing == null) {
                            wearing = new ItemStack(Material.AIR);
                        }

                        toReload.addAll(stateManager.changeItemInSlot(user, player, wearing, armorType.getEquipmentSlot(), false, false, false));
                    }

                    // Reload after all slots are processed to prevent redundant reloads
                    stateManager.reloadIdentifiers(user, toReload);
                }
            }
        };
        plugin.getScheduler().timerSync(task, 0L, plugin.configInt(Option.MODIFIER_ARMOR_TIMER_CHECK_PERIOD) * 50L, TimeUnit.MILLISECONDS);
    }

}
