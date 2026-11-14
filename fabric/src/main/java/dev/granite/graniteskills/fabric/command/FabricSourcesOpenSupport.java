package dev.granite.graniteskills.fabric.command;

import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.api.source.XpSource;
import dev.granite.graniteskills.api.util.NumberUtil;
import dev.granite.graniteskills.common.skill.LoadedSkill;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import dev.granite.graniteskills.fabric.menu.FabricGraniteSkillsMenus;
import dev.granite.graniteskills.fabric.menu.FabricXpSourceMenuDisplay;
import dev.granite.graniteskills.fabric.text.FabricGraniteText;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Locale;

/**
 * Shared /sources and {@code openmenu sources} open path: sort + list build, paged **chat** output,
 * and GUI + fallback (**Sprint 69+**).
 */
public final class FabricSourcesOpenSupport {

    /** Same as {@code SourcesMenu.DEF_ITEMS_PER_PAGE} / Bukkit paged chat chunks. */
    public static final int LINES_PER_MESSAGE = 28;

    private FabricSourcesOpenSupport() {}

    public static String sortKey(FabricSourceSortType sort) {
        return sort.name().toLowerCase(Locale.ROOT);
    }

    /**
     * Paged feedback lines (console/RCON, or in-game if the screen failed to open).
     */
    public static void sendChatListing(
            FabricGraniteSkills plugin,
            CommandSourceStack source,
            Locale lo,
            Skill skill,
            List<XpSource> list,
            FabricSourceSortType sort) {
        int totalPages = (list.size() + LINES_PER_MESSAGE - 1) / LINES_PER_MESSAGE;
        for (int start = 0; start < list.size(); start += LINES_PER_MESSAGE) {
            int end = Math.min(start + LINES_PER_MESSAGE, list.size());
            int page = (start / LINES_PER_MESSAGE) + 1;
            MutableComponent out = Component.empty();
            if (start == 0) {
                out.append(
                        FabricGraniteText.key(
                                "graniteskills.granite.command.sources.header",
                                skill.getDisplayName(lo, false),
                                sortKey(sort),
                                list.size(),
                                page,
                                totalPages));
            } else {
                out.append(
                        FabricGraniteText.key(
                                "graniteskills.granite.command.sources.continuation", page, totalPages));
            }
            for (int i = start; i < end; i++) {
                int index = i + 1;
                XpSource s = list.get(i);
                out.append(Component.literal("\n"));
                out.append(
                        FabricGraniteText.key(
                                "graniteskills.granite.command.sources.line",
                                index,
                                FabricXpSourceMenuDisplay.effectiveRowTitle(plugin, s, lo),
                                NumberUtil.format2(s.getXp()),
                                s.getId().toString(),
                                s.getType().getId().toString()));
            }
            MutableComponent message = out;
            source.sendSuccess(() -> message, false);
        }
    }

    /**
     * In-game: open the sources GUI, or on failure the chat list. **Console path:** {@code
     * menuPlayer} is null → chat to {@code feedbackSource} with {@code lo}.
     */
    public static int runGuiOrChatFor(
            FabricGraniteSkills p,
            CommandSourceStack feedbackSource,
            ServerPlayer menuPlayer,
            Locale lo,
            Skill skill,
            FabricSourceSortType sort,
            int returnToSkillsListPage) {
        LoadedSkill loaded = p.getSkillManager().getSkill(skill);
        List<XpSource> list = FabricSourcesListSupport.buildSorted(p, loaded, sort, lo);
        if (list.isEmpty()) {
            long versionValidCount = loaded.sources().stream().filter(XpSource::isVersionValid).count();
            if (shouldShowVisibleButNotRenderableWarning(versionValidCount, list.size())) {
                feedbackSource.sendSuccess(
                        () -> Component.literal(
                                "No visible XP sources are currently renderable for "
                                        + skill.getDisplayName(lo, false)
                                        + ". Version-valid sources found: "
                                        + versionValidCount
                                        + ". Run /sources diagnose "
                                        + skill.getId().getSimpleName()),
                        false);
            } else {
                feedbackSource.sendSuccess(
                        () -> FabricGraniteText.key(
                                "graniteskills.granite.command.sources.empty",
                                skill.getDisplayName(lo, false)),
                        false);
            }
            return 1;
        }
        if (menuPlayer == null) {
            sendChatListing(p, feedbackSource, lo, skill, list, sort);
            return 1;
        }
        try {
            FabricGraniteSkillsMenus.openSources(
                    skill, menuPlayer, p, sort, 0, returnToSkillsListPage);
        } catch (Throwable t) {
            p.logger().warn("Failed to open sources menu", t);
            feedbackSource.sendSuccess(
                    () -> FabricGraniteText.key("graniteskills.granite.command.sources.menu_fallback"),
                    false);
            sendChatListing(p, feedbackSource, lo, skill, list, sort);
        }
        return 1;
    }

    /**
     * For {@code /openmenu sources} when the list is already known <strong>non-empty</strong>. Opens
     * the GUI to {@code menuPlayer} or, on exception, the chat listing to the executor. Returns
     * whether the screen was opened (true) or fallback to chat (false) — the caller can choose
     * follow-up “opened” lines.
     */
    public static boolean tryOpenPlayerGuiWithChatFallback(
            FabricGraniteSkills p,
            CommandSourceStack feedbackSource,
            ServerPlayer menuPlayer,
            Locale lo,
            Skill skill,
            List<XpSource> list,
            FabricSourceSortType sort,
            int returnToSkillsListPage) {
        try {
            FabricGraniteSkillsMenus.openSources(
                    skill, menuPlayer, p, sort, 0, returnToSkillsListPage);
            return true;
        } catch (Throwable t) {
            p.logger().warn("Failed to open sources menu", t);
            feedbackSource.sendSuccess(
                    () -> FabricGraniteText.key("graniteskills.granite.command.sources.menu_fallback"),
                    false);
            sendChatListing(p, feedbackSource, lo, skill, list, sort);
            return false;
        }
    }

    static boolean shouldShowVisibleButNotRenderableWarning(long versionValidCount, int visibleCount) {
        return visibleCount == 0 && versionValidCount > 0;
    }
}
