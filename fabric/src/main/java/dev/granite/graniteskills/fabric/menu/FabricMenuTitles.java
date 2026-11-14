package dev.granite.graniteskills.fabric.menu;

import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.common.message.MessageProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.Locale;

/**
 * Sprint 119 — centralized Granite Skills <strong>menu title</strong> styling (inventory titles only).
 *
 * <p>Rule: primary = {@link ChatFormatting#DARK_GRAY} + bold (legacy {@code §8§l} / MiniMessage {@code
 * <dark_gray><bold>}); secondary / context = {@link ChatFormatting#GRAY} ({@code §7} / {@code <gray>}); optional
 * page suffix = gray. Do not use gold for primary menu titles.
 */
public final class FabricMenuTitles {

    private FabricMenuTitles() {}

    public static Component primaryPlain(String content) {
        if (content == null || content.isEmpty()) {
            return Component.empty();
        }
        return Component.literal(content).withStyle(s -> s.withBold(true).withColor(ChatFormatting.DARK_GRAY));
    }

    public static Component secondaryPlain(String content) {
        if (content == null || content.isEmpty()) {
            return Component.empty();
        }
        return Component.literal(content).withStyle(s -> s.withColor(ChatFormatting.GRAY));
    }

    /** Primary (dark_gray bold) then a space then secondary (gray). */
    public static Component combinePrimaryThenSecondary(String primaryPlainText, String secondaryPlainText) {
        MutableComponent t = Component.empty();
        t.append(primaryPlain(primaryPlainText));
        if (secondaryPlainText != null && !secondaryPlainText.isEmpty()) {
            t.append(Component.literal(" "));
            t.append(secondaryPlain(secondaryPlainText));
        }
        return t;
    }

    /** Secondary (gray) prefix immediately followed by primary (dark_gray bold), e.g. {@code Stat: Name}. */
    public static Component combineSecondaryThenPrimary(String secondaryGrayFirst, String primaryPlainAfter) {
        MutableComponent t = Component.empty();
        t.append(secondaryPlain(secondaryGrayFirst));
        t.append(primaryPlain(primaryPlainAfter));
        return t;
    }

    /**
     * Leaderboard-style lines from lang, e.g. {@code Leaderboard — Power}. Splits on {@code " — "} (space, em dash,
     * space); if missing, the whole line is primary-only.
     */
    public static Component fromLeaderboardContextLine(String line) {
        if (line == null || line.isEmpty()) {
            return Component.empty();
        }
        String sep = " — ";
        int idx = line.indexOf(sep);
        if (idx < 0) {
            return primaryPlain(line);
        }
        String left = line.substring(0, idx);
        String right = line.substring(idx + sep.length());
        return combinePrimaryThenSecondary(left, right);
    }

    public static Component appendPageSuffixGray(Component baseTitle, int pageIndex, int maxPageIndex) {
        if (maxPageIndex <= 0) {
            return baseTitle;
        }
        String pattern = Language.getInstance().getOrDefault("graniteskills.granite.menu.list_paging_subtitle");
        if (pattern == null || pattern.isBlank()) {
            pattern = " (Page %1$s of %2$s)";
        }
        String suffix = String.format(Locale.ROOT, pattern, pageIndex + 1, maxPageIndex + 1);
        MutableComponent t = Component.empty();
        t.append(baseTitle);
        t.append(Component.literal(suffix).withStyle(s -> s.withColor(ChatFormatting.GRAY)));
        return t;
    }

    /**
     * Built-in / registry menu title from {@code en_us} / language map: entire string is styled as primary (skills hub,
     * stats hub, etc.).
     */
    public static Component granitePrimaryTitleFromKey(String translationKey) {
        String pattern = Language.getInstance().getOrDefault(translationKey);
        if (pattern == null || pattern.isEmpty() || pattern.equals(translationKey)) {
            return Component.literal(translationKey);
        }
        return primaryPlain(pattern);
    }

    public static Component levelProgressionTitle(MessageProvider msg, Skill skill, Locale locale, int pageIndex, int maxPageIndex) {
        String skillDisp = msg.getSkillDisplayName(skill, locale, true);
        String levels = Language.getInstance().getOrDefault("graniteskills.granite.menu.level_progression_levels_suffix");
        if (levels == null || levels.isBlank()) {
            levels = "Levels";
        }
        Component base = combinePrimaryThenSecondary(skillDisp, levels);
        return appendPageSuffixGray(base, pageIndex, maxPageIndex);
    }

    public static Component statInfoTitle(String statDisplayName) {
        String suffix = Language.getInstance().getOrDefault("graniteskills.granite.menu.stat_info_title_suffix");
        if (suffix == null || suffix.isBlank()) {
            suffix = "Stat";
        }
        return combinePrimaryThenSecondary(statDisplayName, suffix);
    }

    public static Component abilitiesMenuTitle(MessageProvider msg, Skill skill, Locale locale) {
        String skillDisp = msg.getSkillDisplayName(skill, locale, true);
        String suffix = Language.getInstance().getOrDefault("graniteskills.granite.menu.abilities_title_suffix");
        if (suffix == null || suffix.isBlank()) {
            suffix = "\u00b7 Abilities";
        }
        return combinePrimaryThenSecondary(skillDisp, suffix);
    }

    /** Sprint 120: short title {@code <Skill> Sources}; sort belongs on the sort control, not the window title. */
    public static Component sourcesMenuTitle(String skillDisplayName) {
        String suffix = Language.getInstance().getOrDefault("graniteskills.granite.menu.sources_title_suffix");
        if (suffix == null || suffix.isBlank()) {
            suffix = "Sources";
        }
        return combinePrimaryThenSecondary(skillDisplayName, suffix);
    }

    public static Component unclaimedMenuTitlePlain(String plainTitle) {
        return primaryPlain(plainTitle);
    }

    /**
     * Unclaimed rewards inventory title: plain string from {@code graniteskills.granite.menu.unclaimed_items_title},
     * styled as primary, plus gray paging suffix when {@code maxPageIndex > 0}.
     */
    public static Component unclaimedItemsTitle(int pageIndex, int maxPageIndex) {
        String plain = Language.getInstance().getOrDefault("graniteskills.granite.menu.unclaimed_items_title");
        if (plain == null || plain.isBlank()) {
            plain = "Claim Items";
        }
        return appendPageSuffixGray(primaryPlain(plain), pageIndex, maxPageIndex);
    }
}
