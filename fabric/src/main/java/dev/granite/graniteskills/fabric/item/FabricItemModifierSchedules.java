package dev.granite.graniteskills.fabric.item;

import dev.granite.graniteskills.api.stat.ReloadableIdentifier;
import dev.granite.graniteskills.common.config.Option;
import dev.granite.graniteskills.common.scheduler.TaskRunnable;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Periodic equipment checks mirroring Bukkit {@code ItemListener} and {@code ArmorModifierListener} timers.
 */
public final class FabricItemModifierSchedules {

    private FabricItemModifierSchedules() {
    }

    public static void register(FabricGraniteSkills plugin, FabricItemStateManager stateManager) {
        long itemPeriodMs = Math.max(50L, (long) plugin.configInt(Option.MODIFIER_ITEM_CHECK_PERIOD) * 50L);
        plugin.getScheduler().timerSync(new TaskRunnable() {
            @Override
            public void run() {
                MinecraftServer server = plugin.getServer();
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    User user = plugin.getUserManager().getUser(player.getUUID());
                    ItemStack main = player.getMainHandItem();
                    stateManager.changeItemInSlot(user, player, main, EquipmentSlot.MAINHAND);
                }
            }
        }, 0L, itemPeriodMs, TimeUnit.MILLISECONDS);

        if (plugin.configBoolean(Option.MODIFIER_ITEM_ENABLE_OFF_HAND)) {
            plugin.getScheduler().timerSync(new TaskRunnable() {
                @Override
                public void run() {
                    MinecraftServer server = plugin.getServer();
                    for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                        User user = plugin.getUserManager().getUser(player.getUUID());
                        ItemStack off = player.getOffhandItem();
                        stateManager.changeItemInSlot(user, player, off, EquipmentSlot.OFFHAND);
                    }
                }
            }, 0L, itemPeriodMs, TimeUnit.MILLISECONDS);
        }

        if (plugin.configBoolean(Option.MODIFIER_ARMOR_TIMER_ENABLED)) {
            long armorPeriodMs = Math.max(50L, (long) plugin.configInt(Option.MODIFIER_ARMOR_TIMER_CHECK_PERIOD) * 50L);
            plugin.getScheduler().timerSync(new TaskRunnable() {
                @Override
                public void run() {
                    MinecraftServer server = plugin.getServer();
                    for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                        User user = plugin.getUserManager().getUser(player.getUUID());
                        Set<ReloadableIdentifier> toReload = new HashSet<>();
                        for (EquipmentSlot slot : FabricUserEquipment.TRACKED) {
                            if (slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND) {
                                continue;
                            }
                            ItemStack wearing = player.getItemBySlot(slot);
                            toReload.addAll(stateManager.changeItemInSlot(user, player, wearing, slot, false, false, false));
                        }
                        stateManager.reloadIdentifiers(user, toReload);
                    }
                }
            }, 0L, armorPeriodMs, TimeUnit.MILLISECONDS);
        }
    }
}
