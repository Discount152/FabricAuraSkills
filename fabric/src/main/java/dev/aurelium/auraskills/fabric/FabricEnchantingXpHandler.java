package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.source.SkillSource;
import dev.aurelium.auraskills.api.source.type.EnchantingXpSource;
import dev.aurelium.auraskills.common.source.SourceTypes;
import dev.aurelium.auraskills.common.user.User;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public final class FabricEnchantingXpHandler {

    /** Bukkit typo in {@link dev.aurelium.auraskills.bukkit.source.EnchantingLeveler} — match for config parity. */
    private static final String UNIT_EXP_REQUIREMENT_TYPO = "{sources.units.exp_requirement";

    private FabricEnchantingXpHandler() {
    }

    public static void onEnchantSuccess(ServerPlayer player, EnchantmentMenu handler, int buttonId,
            ItemStack stackBeforeEnchant, int xpLevelBefore) {
        FabricAuraSkills plugin = FabricAuraSkills.getInstance();
        if (plugin == null) {
            return;
        }
        ItemStack after = handler.getSlot(0).getItem();
        if (after.isEmpty()) {
            return;
        }
        dev.aurelium.auraskills.fabric.ability.FabricEnchantingAbilities.tryLuckyTable(
                plugin, player, handler, stackBeforeEnchant);
        SkillSource<EnchantingXpSource> skillSource = findSource(plugin, stackBeforeEnchant);
        if (skillSource == null) {
            return;
        }
        EnchantingXpSource source = skillSource.source();
        Skill skill = skillSource.skill();
        if (FabricXpHandlerUtil.shouldBlockXp(plugin, player, skill, SourceTypes.ENCHANTING)) {
            return;
        }
        double xp = source.getXp();
        String unit = source.getUnit();
        if (unit != null) {
            if (unit.equals("{sources.units.enchant_level}")) {
                int totalAdded = sumAddedEnchantLevels(stackBeforeEnchant, after);
                xp *= totalAdded;
            } else if (unit.equals(UNIT_EXP_REQUIREMENT_TYPO) || unit.equals("{sources.units.exp_requirement}")) {
                int spent = Math.max(0, xpLevelBefore - player.experienceLevel);
                xp *= spent;
            }
        }
        if (xp <= 0) {
            return;
        }
        User user = plugin.getUserManager().getUser(player.getUUID());
        plugin.getLevelManager().addXp(user, skill, source, xp);
    }

    private static SkillSource<EnchantingXpSource> findSource(FabricAuraSkills plugin, ItemStack item) {
        for (SkillSource<EnchantingXpSource> entry : plugin.getSkillManager().getSourcesOfType(EnchantingXpSource.class)) {
            Skill skill = entry.skill();
            if (FabricLootItemMatcher.passesApiItemFilter(plugin, item, entry.source().getItem(), skill)) {
                return entry;
            }
        }
        return null;
    }

    private static int sumAddedEnchantLevels(ItemStack before, ItemStack after) {
        return diffEnchantComponent(before, after, DataComponents.ENCHANTMENTS)
                + diffEnchantComponent(before, after, DataComponents.STORED_ENCHANTMENTS);
    }

    private static int diffEnchantComponent(ItemStack before, ItemStack after,
            net.minecraft.core.component.DataComponentType<ItemEnchantments> type) {
        ItemEnchantments a = after.get(type);
        if (a == null || a.isEmpty()) {
            return 0;
        }
        ItemEnchantments b = before.get(type);
        int sum = 0;
        for (var e : a.entrySet()) {
            Holder<Enchantment> ench = e.getKey();
            int afterLv = e.getIntValue();
            int beforeLv = b != null ? b.getLevel(ench) : 0;
            sum += Math.max(0, afterLv - beforeLv);
        }
        return sum;
    }
}
