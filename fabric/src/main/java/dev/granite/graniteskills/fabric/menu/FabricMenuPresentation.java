package dev.granite.graniteskills.fabric.menu;

import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.api.source.XpSource;
import dev.granite.graniteskills.api.stat.Stat;
import dev.granite.graniteskills.api.util.NumberUtil;
import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import dev.granite.graniteskills.common.message.MessageProvider;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import dev.granite.graniteskills.fabric.FabricSourceMenuItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.ResolvableProfile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class FabricMenuPresentation {

    public static final Map<String, String> STAT_HUB_SHORT_DESCRIPTIONS = Map.ofEntries(
            Map.entry("strength", "Increases physical damage."),
            Map.entry("crit_chance", "Increases chance to land critical hits."),
            Map.entry("crit_damage", "Increases damage dealt by critical hits."),
            Map.entry("health", "Increases your maximum HP."),
            Map.entry("regeneration", "Improves natural health recovery."),
            Map.entry("toughness", "Reduces incoming damage."),
            Map.entry("luck", "Improves chance-based rewards."),
            Map.entry("wisdom", "Improves magical and experience-based scaling."),
            Map.entry("speed", "Increases movement speed."));

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
            Map.entry("alchemy", Items.BREWING_STAND),
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

    public static String statHubShortDescription(Stat stat) {
        if (stat == null) {
            return "";
        }
        return STAT_HUB_SHORT_DESCRIPTIONS.getOrDefault(
                stat.getId().getSimpleName().toLowerCase(Locale.ROOT), "");
    }

    /**
     * Lifetime XP for hub line 1: progress within the current level plus XP thresholds already cleared for this skill.
     * Matches how {@link dev.granite.graniteskills.common.level.LevelManager#levelUpSkill} consumes
     * {@code getXpRequired(skill, newLevel)} per tier.
     */
    public static double totalSkillXpForHub(User user, Skill skill, GraniteSkillsPlugin plugin) {
        int level = user.getSkillLevel(skill);
        int start = plugin.config().getStartLevel();
        double total = user.getSkillXp(skill);
        for (int l = level; l > start; l--) {
            total += plugin.getXpRequirements().getXpRequired(skill, l);
        }
        return total;
    }

    public static void applySkillsHubRow(
            ItemStack stack,
            Skill skill,
            User user,
            FabricGraniteSkills plugin,
            MessageProvider msg,
            Locale locale) {
        stack.set(
                DataComponents.ITEM_NAME,
                FabricMenuLoreFormatting.formatMenuLine(plugin, msg.getSkillDisplayName(skill, locale, false)));
        String xpInLevel = NumberUtil.format2(user.getSkillXp(skill));
        String xpTotal = NumberUtil.format2(totalSkillXpForHub(user, skill, plugin));
        int level = user.getSkillLevel(skill);
        List<Component> lines = new ArrayList<>();
        lines.add(
                FabricMenuLoreFormatting.formatMenuLine(
                        plugin, "Level " + level + " \u00b7 XP " + xpTotal));
        lines.add(FabricMenuLoreFormatting.formatMenuLine(plugin, xpInLevel + " in this level"));
        lines.add(Component.empty());
        lines.add(FabricMenuLoreFormatting.formatMenuLine(plugin, "\u25b8 Levels"));
        stack.set(DataComponents.LORE, new ItemLore(lines));
    }

    public static void applyStatsHubRow(
            ItemStack stack, Stat stat, User user, FabricGraniteSkills plugin, MessageProvider msg, Locale locale) {
        stack.set(DataComponents.ITEM_NAME, Component.literal(msg.getStatDisplayName(stat, locale, false)));
        String value = FabricStatsMenuGui.formatStatLevel(stat, user, plugin, locale);
        String desc = statHubShortDescription(stat);
        List<Component> lines = new ArrayList<>();
        lines.add(Component.literal("Value: " + value));
        lines.add(Component.literal(desc));
        lines.add(Component.empty());
        lines.add(Component.literal("\u25b8 Details"));
        stack.set(DataComponents.LORE, new ItemLore(lines));
    }

    public static ItemStack playerHeadWithTemplateDecor(ItemStack templateDecor, ServerPlayer player) {
        ItemStack head = new ItemStack(Items.PLAYER_HEAD);
        head.set(DataComponents.PROFILE, ResolvableProfile.createResolved(player.getGameProfile()));
        if (templateDecor.has(DataComponents.ITEM_NAME)) {
            head.set(DataComponents.ITEM_NAME, templateDecor.get(DataComponents.ITEM_NAME));
        }
        if (templateDecor.has(DataComponents.LORE)) {
            head.set(DataComponents.LORE, templateDecor.get(DataComponents.LORE));
        }
        FabricMenuTooltipPresentation.suppressVanillaMechanicsTooltip(head);
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
            item = Items.DYE.lime();
        } else if (displayLevel == userLevel) {
            item = Items.GOLD_INGOT;
        } else {
            item = Items.DYE.gray();
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
        if (templateRow.has(DataComponents.ITEM_NAME)) {
            icon.set(DataComponents.ITEM_NAME, templateRow.get(DataComponents.ITEM_NAME));
        }
        if (templateRow.has(DataComponents.LORE)) {
            icon.set(DataComponents.LORE, templateRow.get(DataComponents.LORE));
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
        if (templateRow.has(DataComponents.ITEM_NAME)) {
            out.set(DataComponents.ITEM_NAME, templateRow.get(DataComponents.ITEM_NAME));
        }
        if (templateRow.has(DataComponents.LORE)) {
            out.set(DataComponents.LORE, templateRow.get(DataComponents.LORE));
        }
        if (templateRow.has(DataComponents.CUSTOM_MODEL_DATA)) {
            out.set(DataComponents.CUSTOM_MODEL_DATA, templateRow.get(DataComponents.CUSTOM_MODEL_DATA));
        }
        return out;
    }
}
