package dev.granite.graniteskills.fabric.ability;

import dev.granite.graniteskills.api.ability.Abilities;
import dev.granite.graniteskills.api.stat.StatModifier;
import dev.granite.graniteskills.api.stat.Stats;
import dev.granite.graniteskills.api.util.GraniteSkillsModifier.Operation;
import dev.granite.graniteskills.common.scheduler.TaskRunnable;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
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

    public FabricEnchantingAbilities(FabricGraniteSkills plugin) {
        super(plugin, Abilities.XP_CONVERT, Abilities.ENCHANTER, Abilities.XP_WARRIOR, Abilities.ENCHANTED_STRENGTH, Abilities.LUCKY_TABLE);
        plugin.getScheduler().timerSync(new TaskRunnable() {
            @Override
            public void run() {
                refreshEnchantedStrength();
            }
        }, 1, 10, TimeUnit.SECONDS);
    }

    public static void onSkillXp(FabricGraniteSkills plugin, User user, double amount) {
        if (amount <= 0) {
            return;
        }
        FabricEnchantingAbilities self = plugin.getAbilityManager().getAbilityImpl(FabricEnchantingAbilities.class);
        var ability = Abilities.XP_CONVERT;
        ServerPlayer player = dev.granite.graniteskills.fabric.FabricUser.getPlayer(user);
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

    public static void onKill(FabricGraniteSkills plugin, ServerPlayer player, int droppedXp) {
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

    public static void tryLuckyTable(FabricGraniteSkills plugin, ServerPlayer player, EnchantmentMenu menu, ItemStack before) {
        ItemStack after = menu.getSlot(0).getItem();
        applyLuckyTable(plugin, player, before, after, Double.NaN);
    }

    /**
     * Bukkit {@code EnchantingAbilities#luckyTable}: chance to bump each newly added enchant by +1
     * when below max level.
     *
     * @param forcedRoll pass a fixed {@code [0,1)} roll for probes; {@link Double#NaN} uses the ability RNG
     * @return number of enchantments upgraded
     */
    public static int applyLuckyTable(
            FabricGraniteSkills plugin, ServerPlayer player, ItemStack before, ItemStack after, double forcedRoll) {
        if (plugin == null || player == null || before == null || after == null || after.isEmpty()) {
            return 0;
        }
        FabricEnchantingAbilities self = plugin.getAbilityManager().getAbilityImpl(FabricEnchantingAbilities.class);
        var ability = Abilities.LUCKY_TABLE;
        if (self.isDisabled(ability) || self.failsChecks(player, ability)) {
            return 0;
        }
        User user = plugin.getUserManager().getUser(player.getUUID());
        double chance = self.getValue(ability, user) / 100.0;
        ItemEnchantments beforeEnchants = before.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        ItemEnchantments afterEnchants = after.getEnchantments();
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(afterEnchants);
        int upgraded = 0;
        for (Holder<Enchantment> enchantment : afterEnchants.keySet()) {
            int beforeLevel = beforeEnchants.getLevel(enchantment);
            int afterLevel = afterEnchants.getLevel(enchantment);
            if (afterLevel <= beforeLevel || afterLevel >= enchantment.value().getMaxLevel()) {
                continue;
            }
            double roll = Double.isNaN(forcedRoll) ? self.rand.nextDouble() : forcedRoll;
            if (roll < chance) {
                mutable.set(enchantment, afterLevel + 1);
                upgraded++;
            }
        }
        if (upgraded > 0) {
            after.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());
        }
        return upgraded;
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
