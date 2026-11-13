package dev.granite.graniteskills.api.event.item;

import dev.granite.graniteskills.api.item.ModifierType;
import dev.granite.graniteskills.api.stat.ReloadableIdentifier;
import dev.granite.graniteskills.api.user.SkillsUser;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class ItemDisableEvent extends ItemToggleEvent {

    public ItemDisableEvent(Player player, SkillsUser user, ItemStack item, ModifierType type, @NotNull EquipmentSlot slot, Set<ReloadableIdentifier> toReload) {
        super(player, user, item, type, slot, toReload);
    }

}
