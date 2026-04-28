package dev.aurelium.auraskills.fabric.menu;

import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.source.XpSource;
import dev.aurelium.auraskills.api.stat.Stat;
import dev.aurelium.auraskills.fabric.FabricSourceMenuItems;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class FabricMenuPresentation {

    private static final Map<String, Item> SKILL_ROW_ITEMS = Map.ofEntries(
            Map.entry("farming", Items.IRON_HOE),
            Map.entry("foraging", Items.IRON_AXE),
            Map.entry("mining", Items.IRON_PICKAXE),
            Map.entry("fishing", Items.FISHING_ROD),
            Map.entry("excavation", Items.IRON_SHOVEL),
            Map.entry("archery", Items.BOW),
            Map.entry("fighting", Items.IRON_SWORD),
            Map.entry("defense", Items.IRON_CHESTPLATE),
            Map.entry("agility", Items.FEATHER),
            Map.entry("endurance", Items.BREAD),
            Map.entry("alchemy", Items.POTION),
            Map.entry("enchanting", Items.ENCHANTING_TABLE),
            Map.entry("sorcery", Items.BLAZE_ROD),
            Map.entry("healing", Items.GOLDEN_CARROT),
            Map.entry("forging", Items.ANVIL));

    private static final Map<String, Item> STAT_ROW_ITEMS = new HashMap<>();

    static {
        STAT_ROW_ITEMS.put("strength", Items.DIAMOND_SWORD);
        STAT_ROW_ITEMS.put("health", Items.GOLDEN_APPLE);
        STAT_ROW_ITEMS.put("regeneration", Items.GOLDEN_CARROT);
        STAT_ROW_ITEMS.put("luck", Items.EMERALD);
        STAT_ROW_ITEMS.put("wisdom", Items.BOOK);
        STAT_ROW_ITEMS.put("toughness", Items.SHIELD);
        STAT_ROW_ITEMS.put("crit_chance", Items.TARGET);
        STAT_ROW_ITEMS.put("crit_damage", Items.NETHERITE_AXE);
        STAT_ROW_ITEMS.put("speed", Items.SUGAR);
    }

    private FabricMenuPresentation() {
    }

    /** Skills menu stats utility: player skin head + template name/lore (Sprint 113). */
    public static ItemStack playerHeadWithTemplateDecor(ItemStack templateDecor, ServerPlayerEntity player) {
        ItemStack head = new ItemStack(Items.PLAYER_HEAD);
        head.set(DataComponentTypes.PROFILE, ProfileComponent.ofStatic(player.getGameProfile()));
        if (templateDecor.contains(DataComponentTypes.ITEM_NAME)) {
            head.set(DataComponentTypes.ITEM_NAME, templateDecor.get(DataComponentTypes.ITEM_NAME));
        }
        if (templateDecor.contains(DataComponentTypes.LORE)) {
            head.set(DataComponentTypes.LORE, templateDecor.get(DataComponentTypes.LORE));
        }
        return head;
    }

    public static ItemStack applySkillRowIcon(ItemStack templateRow, Skill skill) {
        return skillRowItem(skill).map(item -> withRowItemPreservingDisplay(templateRow, item)).orElse(templateRow);
    }

    public static ItemStack applyStatRowIcon(ItemStack templateRow, Stat stat) {
        return statRowItem(stat).map(item -> withRowItemPreservingDisplay(templateRow, item)).orElse(templateRow);
    }

    public static ItemStack applyStatPanelIcon(ItemStack templateRow, Stat stat) {
        return applyStatRowIcon(templateRow, stat);
    }

    public static ItemStack levelProgressionRowIcon(ItemStack templateBuilt, int userLevel, int displayLevel) {
        Item item;
        if (displayLevel < userLevel) {
            item = Items.LIME_DYE;
        } else if (displayLevel == userLevel) {
            item = Items.GOLD_INGOT;
        } else {
            item = Items.GRAY_DYE;
        }
        return withRowItemPreservingDisplay(templateBuilt, item);
    }

    public static ItemStack mergeSourceRowFromRegistry(
            FabricSourceMenuItems sourceMenuItems, XpSource src, ItemStack templateRow) {
        ItemStack reg = sourceMenuItems.getMenuItem(src);
        if (reg == null || reg.isEmpty()) {
            return templateRow;
        }
        ItemStack icon = reg.copy();
        icon.setCount(Math.max(1, templateRow.getCount()));
        if (templateRow.contains(DataComponentTypes.ITEM_NAME)) {
            icon.set(DataComponentTypes.ITEM_NAME, templateRow.get(DataComponentTypes.ITEM_NAME));
        }
        if (templateRow.contains(DataComponentTypes.LORE)) {
            icon.set(DataComponentTypes.LORE, templateRow.get(DataComponentTypes.LORE));
        }
        return icon;
    }

    private static Optional<Item> skillRowItem(Skill skill) {
        if (skill == null) {
            return Optional.empty();
        }
        String key = skill.getId().getSimpleName().toLowerCase(Locale.ROOT);
        return Optional.ofNullable(SKILL_ROW_ITEMS.get(key));
    }

    private static Optional<Item> statRowItem(Stat stat) {
        if (stat == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(STAT_ROW_ITEMS.get(stat.getId().getSimpleName().toLowerCase(Locale.ROOT)));
    }

    static ItemStack withRowItemPreservingDisplay(ItemStack templateRow, Item newItem) {
        ItemStack out = new ItemStack(newItem, Math.max(1, templateRow.getCount()));
        if (templateRow.contains(DataComponentTypes.ITEM_NAME)) {
            out.set(DataComponentTypes.ITEM_NAME, templateRow.get(DataComponentTypes.ITEM_NAME));
        }
        if (templateRow.contains(DataComponentTypes.LORE)) {
            out.set(DataComponentTypes.LORE, templateRow.get(DataComponentTypes.LORE));
        }
        if (templateRow.contains(DataComponentTypes.CUSTOM_MODEL_DATA)) {
            out.set(DataComponentTypes.CUSTOM_MODEL_DATA, templateRow.get(DataComponentTypes.CUSTOM_MODEL_DATA));
        }
        return out;
    }
}
