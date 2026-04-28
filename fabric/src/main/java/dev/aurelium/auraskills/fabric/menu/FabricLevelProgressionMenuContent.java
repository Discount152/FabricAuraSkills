package dev.aurelium.auraskills.fabric.menu;

import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.util.NumberUtil;
import dev.aurelium.auraskills.common.level.XpRequirements;
import dev.aurelium.auraskills.common.message.MessageProvider;
import dev.aurelium.auraskills.fabric.text.FabricGraniteText;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

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
                    granitePlain("auraskills.granite.menu.level_progression_status_unlocked"));
            m.put(
                    "level_detail",
                    granitePlain(
                            "auraskills.granite.menu.level_progression_row_xp_required", xpTierCompleted));
        } else if (displayLevel == userLevel) {
            m.put(
                    "level_status",
                    granitePlain("auraskills.granite.menu.level_progression_status_current"));
            if (maxed) {
                m.put(
                        "level_detail",
                        granitePlain("auraskills.granite.menu.level_progression_row_maxed_compact"));
            } else {
                int reqNext = xpReqs.getXpRequired(skill, userLevel + 1);
                m.put(
                        "level_detail",
                        granitePlain(
                                "auraskills.granite.menu.level_progression_row_progress",
                                NumberUtil.format2(currentXp),
                                NumberUtil.format2(reqNext)));
            }
        } else {
            m.put(
                    "level_status",
                    granitePlain("auraskills.granite.menu.level_progression_status_locked"));
            m.put(
                    "level_detail",
                    granitePlain(
                            "auraskills.granite.menu.level_progression_row_xp_required", xpTierLocked));
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

    public static Text appendGrayPagingSubtitle(Text baseTitle, int page, int maxPage) {
        if (maxPage <= 0) {
            return baseTitle;
        }
        String pattern =
                net.minecraft.util.Language.getInstance().get("auraskills.granite.menu.list_paging_subtitle");
        if (pattern == null || pattern.isBlank()) {
            pattern = " (Page %1$s of %2$s)";
        }
        String suffix = String.format(Locale.ROOT, pattern, page + 1, maxPage + 1);
        MutableText t = Text.empty();
        t.append(baseTitle);
        t.append(Text.literal(suffix).styled(s -> s.withColor(Formatting.GRAY)));
        return t;
    }

    public static Text builtInMenuTitle(MessageProvider msg, Skill skill, Locale locale, int page, int maxPage) {
        MutableText t = Text.empty();
        t.append(
                Text.literal(msg.getSkillDisplayName(skill, locale, true))
                        .styled(s -> s.withBold(true).withColor(Formatting.GOLD)));
        t.append(Text.literal(" Levels").styled(s -> s.withColor(Formatting.GRAY)));
        if (maxPage > 0) {
            String pattern =
                    net.minecraft.util.Language.getInstance().get("auraskills.granite.menu.list_paging_subtitle");
            if (pattern == null || pattern.isBlank()) {
                pattern = " (Page %1$s of %2$s)";
            }
            String suffix = String.format(Locale.ROOT, pattern, page + 1, maxPage + 1);
            t.append(Text.literal(suffix).styled(s -> s.withColor(Formatting.GRAY)));
        }
        return t;
    }

    public static void applyQuietMenuFiller(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        stack.set(DataComponentTypes.ITEM_NAME, Text.literal(" "));
        FabricMenuTooltipPresentation.suppressVanillaMechanicsTooltip(stack);
    }
}