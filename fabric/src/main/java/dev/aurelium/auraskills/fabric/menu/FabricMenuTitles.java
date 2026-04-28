package dev.aurelium.auraskills.fabric.menu;

import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.common.message.MessageProvider;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Language;

import java.util.Locale;

/**
 * Sprint 119 — centralized Granite Skills <strong>menu title</strong> styling (inventory titles only).
 *
 * <p>Rule: primary = {@link Formatting#DARK_GRAY} + bold (legacy {@code §8§l} / MiniMessage {@code
 * <dark_gray><bold>}); secondary / context = {@link Formatting#GRAY} ({@code §7} / {@code <gray>}); optional
 * page suffix = gray. Do not use gold for primary menu titles.
 */
public final class FabricMenuTitles {

    private FabricMenuTitles() {}

    public static Text primaryPlain(String content) {
        if (content == null || content.isEmpty()) {
            return Text.empty();
        }
        return Text.literal(content).styled(s -> s.withBold(true).withColor(Formatting.DARK_GRAY));
    }

    public static Text secondaryPlain(String content) {
        if (content == null || content.isEmpty()) {
            return Text.empty();
        }
        return Text.literal(content).styled(s -> s.withColor(Formatting.GRAY));
    }

    /** Primary (dark_gray bold) then a space then secondary (gray). */
    public static Text combinePrimaryThenSecondary(String primaryPlainText, String secondaryPlainText) {
        MutableText t = Text.empty();
        t.append(primaryPlain(primaryPlainText));
        if (secondaryPlainText != null && !secondaryPlainText.isEmpty()) {
            t.append(Text.literal(" "));
            t.append(secondaryPlain(secondaryPlainText));
        }
        return t;
    }

    /** Secondary (gray) prefix immediately followed by primary (dark_gray bold), e.g. {@code Stat: Name}. */
    public static Text combineSecondaryThenPrimary(String secondaryGrayFirst, String primaryPlainAfter) {
        MutableText t = Text.empty();
        t.append(secondaryPlain(secondaryGrayFirst));
        t.append(primaryPlain(primaryPlainAfter));
        return t;
    }

    /**
     * Leaderboard-style lines from lang, e.g. {@code Leaderboard — Power}. Splits on {@code " — "} (space, em dash,
     * space); if missing, the whole line is primary-only.
     */
    public static Text fromLeaderboardContextLine(String line) {
        if (line == null || line.isEmpty()) {
            return Text.empty();
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

    public static Text appendPageSuffixGray(Text baseTitle, int pageIndex, int maxPageIndex) {
        if (maxPageIndex <= 0) {
            return baseTitle;
        }
        String pattern = Language.getInstance().get("auraskills.granite.menu.list_paging_subtitle");
        if (pattern == null || pattern.isBlank()) {
            pattern = " (Page %1$s of %2$s)";
        }
        String suffix = String.format(Locale.ROOT, pattern, pageIndex + 1, maxPageIndex + 1);
        MutableText t = Text.empty();
        t.append(baseTitle);
        t.append(Text.literal(suffix).styled(s -> s.withColor(Formatting.GRAY)));
        return t;
    }

    /**
     * Built-in / registry menu title from {@code en_us} / language map: entire string is styled as primary (skills hub,
     * stats hub, etc.).
     */
    public static Text granitePrimaryTitleFromKey(String translationKey) {
        String pattern = Language.getInstance().get(translationKey);
        if (pattern == null || pattern.isEmpty() || pattern.equals(translationKey)) {
            return Text.literal(translationKey);
        }
        return primaryPlain(pattern);
    }

    public static Text levelProgressionTitle(MessageProvider msg, Skill skill, Locale locale, int pageIndex, int maxPageIndex) {
        String skillDisp = msg.getSkillDisplayName(skill, locale, true);
        String levels = Language.getInstance().get("auraskills.granite.menu.level_progression_levels_suffix");
        if (levels == null || levels.isBlank()) {
            levels = "Levels";
        }
        Text base = combinePrimaryThenSecondary(skillDisp, levels);
        return appendPageSuffixGray(base, pageIndex, maxPageIndex);
    }

    public static Text statInfoTitle(String statDisplayName) {
        String suffix = Language.getInstance().get("auraskills.granite.menu.stat_info_title_suffix");
        if (suffix == null || suffix.isBlank()) {
            suffix = "Stat";
        }
        return combinePrimaryThenSecondary(statDisplayName, suffix);
    }

    public static Text abilitiesMenuTitle(MessageProvider msg, Skill skill, Locale locale) {
        String skillDisp = msg.getSkillDisplayName(skill, locale, true);
        String suffix = Language.getInstance().get("auraskills.granite.menu.abilities_title_suffix");
        if (suffix == null || suffix.isBlank()) {
            suffix = "\u00b7 Abilities";
        }
        return combinePrimaryThenSecondary(skillDisp, suffix);
    }

    /** Sprint 120: short title {@code <Skill> Sources}; sort belongs on the sort control, not the window title. */
    public static Text sourcesMenuTitle(String skillDisplayName) {
        String suffix = Language.getInstance().get("auraskills.granite.menu.sources_title_suffix");
        if (suffix == null || suffix.isBlank()) {
            suffix = "Sources";
        }
        return combinePrimaryThenSecondary(skillDisplayName, suffix);
    }

    public static Text unclaimedMenuTitlePlain(String plainTitle) {
        return primaryPlain(plainTitle);
    }

    /**
     * Unclaimed rewards inventory title: plain string from {@code auraskills.granite.menu.unclaimed_items_title},
     * styled as primary, plus gray paging suffix when {@code maxPageIndex > 0}.
     */
    public static Text unclaimedItemsTitle(int pageIndex, int maxPageIndex) {
        String plain = Language.getInstance().get("auraskills.granite.menu.unclaimed_items_title");
        if (plain == null || plain.isBlank()) {
            plain = "Claim Items";
        }
        return appendPageSuffixGray(primaryPlain(plain), pageIndex, maxPageIndex);
    }
}
