package dev.aurelium.auraskills.fabric.item;

import com.google.common.collect.Sets;
import dev.aurelium.auraskills.api.item.ModifierType;
import dev.aurelium.auraskills.api.skill.Multiplier;
import dev.aurelium.auraskills.api.stat.ReloadableIdentifier;
import dev.aurelium.auraskills.api.stat.StatModifier;
import dev.aurelium.auraskills.api.trait.TraitModifier;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.FabricUser;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Fabric counterpart of Bukkit {@code ItemStateManager}: swap item modifiers when equipment changes.
 */
public final class FabricItemStateManager {

    private final FabricAuraSkills plugin;

    public FabricItemStateManager(FabricAuraSkills plugin) {
        this.plugin = plugin;
    }

    public void changeItemInSlot(User userObj, ServerPlayer player, @NotNull ItemStack afterItem, EquipmentSlot slot) {
        changeItemInSlot(userObj, player, afterItem, slot, true, false, false);
    }

    public Set<ReloadableIdentifier> changeItemInSlot(User userObj, ServerPlayer player, @NotNull ItemStack afterItem,
            EquipmentSlot slot, boolean reloadIds, boolean force, boolean modify) {
        if (!(userObj instanceof FabricUser user)) {
            return Set.of();
        }
        FabricUserEquipment equipment = user.getFabricEquipment();
        ItemStack beforeItem = equipment.getSlot(slot);
        if (!force && ItemStack.matches(afterItem, beforeItem)) {
            return Set.of();
        }

        Set<ReloadableIdentifier> toReload = Sets.newConcurrentHashSet();
        ModifierType modType = FabricSkillsItemReader.modifierTypeForEquipmentSlot(slot);
        boolean offhand = slot == EquipmentSlot.OFFHAND;

        if (!beforeItem.isEmpty()) {
            toReload.addAll(removeItem(beforeItem, user, player, slot, modType, offhand));
        }
        if (!afterItem.isEmpty()) {
            toReload.addAll(addItem(afterItem, user, player, slot, modType, offhand));
        }

        equipment.setSlot(slot, afterItem.copy());

        if (reloadIds) {
            reloadIdentifiers(user, toReload);
        }
        return toReload;
    }

    public void reloadIdentifiers(User user, Set<ReloadableIdentifier> toReload) {
        for (ReloadableIdentifier identifier : toReload) {
            plugin.getStatManager().reload(user, identifier);
        }
    }

    private Set<ReloadableIdentifier> removeItem(ItemStack item, FabricUser user, ServerPlayer player,
            EquipmentSlot slot, ModifierType modType, boolean offhand) {
        Set<ReloadableIdentifier> toReload = Sets.newConcurrentHashSet();
        for (StatModifier modifier : FabricSkillsItemReader.getStatModifiers(plugin, item, modType, offhand)) {
            user.removeStatModifier(modifier.name(), false);
            toReload.add(modifier.stat());
        }
        for (TraitModifier modifier : FabricSkillsItemReader.getTraitModifiers(plugin, item, modType, offhand)) {
            user.removeTraitModifier(modifier.name(), false);
            toReload.add(modifier.trait());
        }
        for (Multiplier multiplier : FabricSkillsItemReader.getMultipliers(plugin, item, modType, offhand)) {
            user.removeMultiplier(multiplier.name());
        }
        return toReload;
    }

    private Set<ReloadableIdentifier> addItem(ItemStack item, FabricUser user, ServerPlayer player,
            EquipmentSlot slot, ModifierType modType, boolean offhand) {
        Set<ReloadableIdentifier> toReload = Sets.newConcurrentHashSet();
        if (!FabricSkillsItemReader.meetsRequirements(plugin, player, item, modType)) {
            return toReload;
        }
        for (StatModifier modifier : FabricSkillsItemReader.getStatModifiers(plugin, item, modType, offhand)) {
            user.addStatModifier(modifier, false);
            toReload.add(modifier.stat());
        }
        for (TraitModifier modifier : FabricSkillsItemReader.getTraitModifiers(plugin, item, modType, offhand)) {
            user.addTraitModifier(modifier, false);
            toReload.add(modifier.trait());
        }
        for (Multiplier multiplier : FabricSkillsItemReader.getMultipliers(plugin, item, modType, offhand)) {
            user.addMultiplier(multiplier);
        }
        return toReload;
    }
}
