package dev.aurelium.auraskills.fabric;

import com.google.common.collect.Sets;
import dev.aurelium.auraskills.api.stat.ReloadableIdentifier;
import dev.aurelium.auraskills.common.config.Option;
import dev.aurelium.auraskills.common.modifier.ModifierManager;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.fabric.item.FabricItemModifierSchedules;
import dev.aurelium.auraskills.fabric.item.FabricItemStateManager;
import dev.aurelium.auraskills.fabric.item.FabricUserEquipment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Set;

public final class FabricModifierManager implements ModifierManager {

    private final FabricAuraSkills plugin;
    private final FabricItemStateManager itemStateManager;
    private volatile boolean schedulesRegistered;

    public FabricModifierManager(FabricAuraSkills plugin) {
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
        ServerPlayerEntity player = FabricUser.getPlayer(user);
        if (player == null) {
            return;
        }
        Set<ReloadableIdentifier> toReload = Sets.newConcurrentHashSet();
        toReload.addAll(itemStateManager.changeItemInSlot(user, player, player.getMainHandStack(), EquipmentSlot.MAINHAND, false, true, false));
        if (plugin.configBoolean(Option.MODIFIER_ITEM_ENABLE_OFF_HAND)) {
            toReload.addAll(itemStateManager.changeItemInSlot(user, player, player.getOffHandStack(), EquipmentSlot.OFFHAND, false, true, false));
        }
        for (EquipmentSlot slot : FabricUserEquipment.TRACKED) {
            if (slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND) {
                continue;
            }
            ItemStack armor = player.getEquippedStack(slot);
            toReload.addAll(itemStateManager.changeItemInSlot(user, player, armor, slot, false, true, false));
        }
        if (reload) {
            itemStateManager.reloadIdentifiers(user, toReload);
        }
    }
}
