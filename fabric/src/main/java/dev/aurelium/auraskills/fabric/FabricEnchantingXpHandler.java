package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.source.SkillSource;
import dev.aurelium.auraskills.api.source.type.EnchantingXpSource;
import dev.aurelium.auraskills.common.source.SourceTypes;
import dev.aurelium.auraskills.common.user.User;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.EnchantmentScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;

public final class FabricEnchantingXpHandler {

    /** Bukkit typo in {@link dev.aurelium.auraskills.bukkit.source.EnchantingLeveler} — match for config parity. */
    private static final String UNIT_EXP_REQUIREMENT_TYPO = "{sources.units.exp_requirement";

    private FabricEnchantingXpHandler() {
    }

    /**
     * Called from {@link dev.aurelium.auraskills.fabric.mixin.EnchantmentScreenHandlerMixin} only when
     * {@link EnchantmentScreenHandler#onButtonClick} returns {@code true} for button id 0–2 (Bukkit
     * {@code EnchantItemEvent} @ MONITOR equivalent: successful table enchant only, not preview).
     */
    public static void onEnchantSuccess(ServerPlayerEntity player, EnchantmentScreenHandler handler, int buttonId,
            ItemStack stackBeforeEnchant, int xpLevelBefore) {
        FabricAuraSkills plugin = FabricAuraSkills.getInstance();
        if (plugin == null) {
            return;
        }
        if (player.isSpectator()) {
            return;
        }
        ItemStack after = handler.getSlot(0).getStack();
        if (after.isEmpty()) {
            return;
        }
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
                // Bukkit: EnchantItemEvent#getExpLevelCost() — the chosen row’s level cost, not a raw player delta
                // (avoids misfire if the player’s level changed for another reason in the same tick).
                int spent = resolveExpLevelCost(handler, buttonId, xpLevelBefore, player);
                xp *= spent;
            }
        }
        if (xp <= 0) {
            return;
        }
        User user = plugin.getUserManager().getUser(player.getUuid());
        plugin.getLevelManager().addXp(user, skill, source, xp);
    }

    /**
     * Matches Bukkit {@code EnchantItemEvent#getExpLevelCost()}: the enchantment table row cost for
     * {@code buttonId}, with fallback to {@code xpLevelBefore - currentLevel} if the field is zero.
     */
    private static int resolveExpLevelCost(
            EnchantmentScreenHandler handler, int buttonId, int xpLevelBefore, ServerPlayerEntity player) {
        if (buttonId >= 0 && buttonId < handler.enchantmentPower.length) {
            int fromTable = handler.enchantmentPower[buttonId];
            if (fromTable > 0) {
                return fromTable;
            }
        }
        return Math.max(0, xpLevelBefore - player.experienceLevel);
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
        return diffEnchantComponent(before, after, DataComponentTypes.ENCHANTMENTS)
                + diffEnchantComponent(before, after, DataComponentTypes.STORED_ENCHANTMENTS);
    }

    private static int diffEnchantComponent(ItemStack before, ItemStack after,
            net.minecraft.component.ComponentType<ItemEnchantmentsComponent> type) {
        ItemEnchantmentsComponent a = after.get(type);
        if (a == null || a.isEmpty()) {
            return 0;
        }
        ItemEnchantmentsComponent b = before.get(type);
        int sum = 0;
        for (var e : a.getEnchantmentEntries()) {
            RegistryEntry<Enchantment> ench = e.getKey();
            int afterLv = e.getIntValue();
            int beforeLv = b != null ? b.getLevel(ench) : 0;
            sum += Math.max(0, afterLv - beforeLv);
        }
        return sum;
    }
}
