package dev.granite.graniteskills.fabric.menu;

import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.api.util.NumberUtil;
import dev.granite.graniteskills.common.level.XpRequirements;
import dev.granite.graniteskills.common.message.MessageProvider;
import dev.granite.graniteskills.fabric.text.FabricGraniteText;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class FabricLevelProgressionMenuContent {

    private FabricLevelProgressionMenuContent() {
    }

    public static Map<String, String> globalPlaceholders(
            Skill skill, int pageIndex, int maxPageIndex, Locale locale, MessageProvider msg) {
        Map<String, String> m = new HashMap<>();
        m.put("skill", skill.getId().getSimpleName());
        m.put("skill_display", msg.getSkillDisplayName(skill, locale, true));
        m.put("page", String.valueOf(pageIndex + 1));
        m.put("total_pages", String.valueOf(maxPageIndex + 1));
        return m;
    }

    public static void addLevelRowPlaceholders(
            Map<String, String> m,
            int userLevel,
            int displayLevel,
            double currentXp,
            Skill skill,
            int maxLevel,
            XpRequirements xpReqs) {
        m.put("level", String.valueOf(displayLevel));
        m.put("current_xp", NumberUtil.format2(currentXp));

        boolean maxed = userLevel >= maxLevel || xpReqs.getListSize(skill) <= userLevel - 1;
        String xpTierCompleted =
                displayLevel >= maxLevel
                        ? NumberUtil.format2(xpReqs.getXpRequired(skill, maxLevel))
                        : NumberUtil.format2(xpReqs.getXpRequired(skill, displayLevel + 1));
        String xpTierLocked = NumberUtil.format2(xpReqs.getXpRequired(skill, displayLevel));

        if (displayLevel < userLevel) {
            m.put(
                    "level_status",
                    granitePlain("graniteskills.granite.menu.level_progression_status_unlocked"));
            m.put(
                    "level_detail",
                    granitePlain(
                            "graniteskills.granite.menu.level_progression_row_xp_required", xpTierCompleted));
        } else if (displayLevel == userLevel) {
            m.put(
                    "level_status",
                    granitePlain("graniteskills.granite.menu.level_progression_status_current"));
            if (maxed) {
                m.put(
                        "level_detail",
                        granitePlain("graniteskills.granite.menu.level_progression_row_maxed_compact"));
            } else {
                int reqNext = xpReqs.getXpRequired(skill, userLevel + 1);
                m.put(
                        "level_detail",
                        granitePlain(
                                "graniteskills.granite.menu.level_progression_row_progress",
                                NumberUtil.format2(currentXp),
                                NumberUtil.format2(reqNext)));
            }
        } else {
            m.put(
                    "level_status",
                    granitePlain("graniteskills.granite.menu.level_progression_status_locked"));
            m.put(
                    "level_detail",
                    granitePlain(
                            "graniteskills.granite.menu.level_progression_row_xp_required", xpTierLocked));
        }

        if (displayLevel >= maxLevel) {
            m.put("level_xp_required", NumberUtil.format2(xpReqs.getXpRequired(skill, maxLevel)));
        } else {
            m.put("level_xp_required", NumberUtil.format2(xpReqs.getXpRequired(skill, displayLevel + 1)));
        }
    }

    private static String granitePlain(String key, Object... args) {
        return FabricGraniteText.key(key, args).getString();
    }

    public static Component appendGrayPagingSubtitle(Component baseTitle, int page, int maxPage) {
        if (maxPage <= 0) {
            return baseTitle;
        }
        String pattern =
                net.minecraft.locale.Language.getInstance().getOrDefault("graniteskills.granite.menu.list_paging_subtitle");
        if (pattern == null || pattern.isBlank()) {
            pattern = " (Page %1$s of %2$s)";
        }
        String suffix = String.format(Locale.ROOT, pattern, page + 1, maxPage + 1);
        MutableComponent t = Component.empty();
        t.append(baseTitle);
        t.append(Component.literal(suffix).withStyle(s -> s.withColor(ChatFormatting.GRAY)));
        return t;
    }

    public static Component builtInMenuTitle(MessageProvider msg, Skill skill, Locale locale, int page, int maxPage) {
        MutableComponent t = Component.empty();
        t.append(
                Component.literal(msg.getSkillDisplayName(skill, locale, true))
                        .withStyle(s -> s.withBold(true).withColor(ChatFormatting.GOLD)));
        t.append(Component.literal(" Levels").withStyle(s -> s.withColor(ChatFormatting.GRAY)));
        if (maxPage > 0) {
            String pattern =
                    net.minecraft.locale.Language.getInstance().getOrDefault("graniteskills.granite.menu.list_paging_subtitle");
            if (pattern == null || pattern.isBlank()) {
                pattern = " (Page %1$s of %2$s)";
            }
            String suffix = String.format(Locale.ROOT, pattern, page + 1, maxPage + 1);
            t.append(Component.literal(suffix).withStyle(s -> s.withColor(ChatFormatting.GRAY)));
        }
        return t;
    }

    public static void applyQuietMenuFiller(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        stack.set(DataComponents.ITEM_NAME, Component.literal(" "));
        FabricMenuTooltipPresentation.suppressVanillaMechanicsTooltip(stack);
    }
}
