package dev.granite.graniteskills.fabric;

import com.google.common.collect.Sets;
import dev.granite.graniteskills.api.stat.ReloadableIdentifier;
import dev.granite.graniteskills.common.config.Option;
import dev.granite.graniteskills.common.modifier.ModifierManager;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.fabric.item.FabricItemModifierSchedules;
import dev.granite.graniteskills.fabric.item.FabricItemStateManager;
import dev.granite.graniteskills.fabric.item.FabricUserEquipment;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import java.util.Set;

public final class FabricModifierManager implements ModifierManager {

    private final FabricGraniteSkills plugin;
    private final FabricItemStateManager itemStateManager;
    private volatile boolean schedulesRegistered;

    public FabricModifierManager(FabricGraniteSkills plugin) {
        this.plugin = plugin;
        this.itemStateManager = new FabricItemStateManager(plugin);
    }

    public FabricItemStateManager getItemStateManager() {
        return itemStateManager;
    }

    /** Registers main/offhand (and optional armor) periodic checks; safe to call once at runtime enable. */
    public void registerSchedulesIfNeeded() {
        if (schedulesRegistered) {
            return;
        }
        synchronized (this) {
            if (schedulesRegistered) {
                return;
            }
            FabricItemModifierSchedules.register(plugin, itemStateManager);
            schedulesRegistered = true;
        }
    }

    @Override
    public void applyModifiers(User user, boolean reload) {
        ServerPlayer player = FabricUser.getPlayer(user);
        if (player == null) {
            return;
        }
        Set<ReloadableIdentifier> toReload = Sets.newConcurrentHashSet();
        toReload.addAll(itemStateManager.changeItemInSlot(user, player, player.getMainHandItem(), EquipmentSlot.MAINHAND, false, true, false));
        if (plugin.configBoolean(Option.MODIFIER_ITEM_ENABLE_OFF_HAND)) {
            toReload.addAll(itemStateManager.changeItemInSlot(user, player, player.getOffhandItem(), EquipmentSlot.OFFHAND, false, true, false));
        }
        for (EquipmentSlot slot : FabricUserEquipment.TRACKED) {
            if (slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND) {
                continue;
            }
            ItemStack armor = player.getItemBySlot(slot);
            toReload.addAll(itemStateManager.changeItemInSlot(user, player, armor, slot, false, true, false));
        }
        if (reload) {
            itemStateManager.reloadIdentifiers(user, toReload);
        }
    }
}
