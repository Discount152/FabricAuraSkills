package dev.aurelium.auraskills.fabric.ability;

import dev.aurelium.auraskills.api.ability.Abilities;
import dev.aurelium.auraskills.api.stat.StatModifier;
import dev.aurelium.auraskills.api.stat.Stats;
import dev.aurelium.auraskills.api.util.AuraSkillsModifier.Operation;
import dev.aurelium.auraskills.common.scheduler.TaskRunnable;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.concurrent.TimeUnit;

public final class FabricEnchantingAbilities extends FabricAbilityImpl {

    private static final String STRENGTH_MODIFIER = "AbilityModifier-EnchantedStrength";

    public FabricEnchantingAbilities(FabricAuraSkills plugin) {
        super(plugin, Abilities.XP_CONVERT, Abilities.ENCHANTER, Abilities.XP_WARRIOR, Abilities.ENCHANTED_STRENGTH, Abilities.LUCKY_TABLE);
        plugin.getScheduler().timerSync(new TaskRunnable() {
            @Override
            public void run() {
                refreshEnchantedStrength();
            }
        }, 1, 10, TimeUnit.SECONDS);
    }

    public static void onSkillXp(FabricAuraSkills plugin, User user, double amount) {
        if (amount <= 0) {
            return;
        }
        FabricEnchantingAbilities self = plugin.getAbilityManager().getAbilityImpl(FabricEnchantingAbilities.class);
        var ability = Abilities.XP_CONVERT;
        ServerPlayer player = dev.aurelium.auraskills.fabric.FabricUser.getPlayer(user);
        if (player == null || self.isDisabled(ability) || self.failsChecks(player, ability)) {
            return;
        }
        double value = self.getValue(ability, user);
        if (value <= 0) {
            return;
        }
        double stored = Math.min(user.getAbilityData(ability).getDouble("xp"), value);
        double total = stored + amount;
        int added = (int) (total / value);
        double remainder = total - added * value;
        if (added > 0) {
            player.giveExperiencePoints(added);
        }
        user.getAbilityData(ability).setData("xp", Math.min(remainder, value));
    }

    public static void onKill(FabricAuraSkills plugin, ServerPlayer player, int droppedXp) {
        if (droppedXp <= 0) {
            return;
        }
        FabricEnchantingAbilities self = plugin.getAbilityManager().getAbilityImpl(FabricEnchantingAbilities.class);
        var ability = Abilities.XP_WARRIOR;
        if (self.isDisabled(ability) || self.failsChecks(player, ability)) {
            return;
        }
        User user = plugin.getUserManager().getUser(player.getUUID());
        if (self.rand.nextDouble() < self.getValue(ability, user) / 100.0) {
            player.giveExperiencePoints(droppedXp);
        }
    }

    public static void tryLuckyTable(FabricAuraSkills plugin, ServerPlayer player, EnchantmentMenu menu, ItemStack before) {
        FabricEnchantingAbilities self = plugin.getAbilityManager().getAbilityImpl(FabricEnchantingAbilities.class);
        var ability = Abilities.LUCKY_TABLE;
        if (self.isDisabled(ability) || self.failsChecks(player, ability)) {
            return;
        }
        ItemStack after = menu.getSlot(0).getItem();
        if (after.isEmpty()) {
            return;
        }
        User user = plugin.getUserManager().getUser(player.getUUID());
        double chance = self.getValue(ability, user) / 100.0;
        ItemEnchantments beforeEnchants = before.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        ItemEnchantments afterEnchants = after.getEnchantments();
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(afterEnchants);
        boolean changed = false;
        for (Holder<Enchantment> enchantment : afterEnchants.keySet()) {
            int beforeLevel = beforeEnchants.getLevel(enchantment);
            int afterLevel = afterEnchants.getLevel(enchantment);
            if (afterLevel > beforeLevel && afterLevel < enchantment.value().getMaxLevel()
                    && self.rand.nextDouble() < chance) {
                mutable.set(enchantment, afterLevel + 1);
                changed = true;
            }
        }
        if (changed) {
            after.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());
        }
    }

    private void refreshEnchantedStrength() {
        var ability = Abilities.ENCHANTED_STRENGTH;
        if (plugin.getServer() == null) {
            return;
        }
        for (ServerPlayer player : plugin.getServer().getPlayerList().getPlayers()) {
            User user = plugin.getUserManager().getUser(player.getUUID());
            ItemEnchantments enchants = player.getMainHandItem().getEnchantments();
            if (isDisabled(ability) || failsChecks(player, ability) || enchants.isEmpty()) {
                user.removeStatModifier(STRENGTH_MODIFIER);
                continue;
            }
            int count = 0;
            for (Holder<Enchantment> enchantment : enchants.keySet()) {
                String path = enchantment.unwrapKey().map(key -> key.identifier().getPath()).orElse("");
                if (ability.optionStringList("excluded_enchantments").contains(path)) {
                    continue;
                }
                count++;
            }
            if (count > 0) {
                user.addStatModifier(new StatModifier(STRENGTH_MODIFIER, Stats.STRENGTH,
                        getValue(ability, user) * count, Operation.ADD), false);
            } else {
                user.removeStatModifier(STRENGTH_MODIFIER);
            }
        }
    }
}
