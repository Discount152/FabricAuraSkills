package dev.aurelium.auraskills.fabric.command;

import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.source.XpSource;
import dev.aurelium.auraskills.api.util.NumberUtil;
import dev.aurelium.auraskills.common.skill.LoadedSkill;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.menu.FabricAuraSkillsMenus;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

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
            ServerCommandSource source,
            Locale lo,
            Skill skill,
            List<XpSource> list,
            FabricSourceSortType sort) {
        int totalPages = (list.size() + LINES_PER_MESSAGE - 1) / LINES_PER_MESSAGE;
        for (int start = 0; start < list.size(); start += LINES_PER_MESSAGE) {
            int end = Math.min(start + LINES_PER_MESSAGE, list.size());
            int page = (start / LINES_PER_MESSAGE) + 1;
            MutableText out;
            if (start == 0) {
                out = Text.translatable(
                        "auraskills.granite.command.sources.header",
                        skill.getDisplayName(lo, false),
                        sortKey(sort),
                        list.size(),
                        page,
                        totalPages);
            } else {
                out = Text.translatable(
                        "auraskills.granite.command.sources.continuation", page, totalPages);
            }
            for (int i = start; i < end; i++) {
                int index = i + 1;
                XpSource s = list.get(i);
                out.append(Text.literal("\n"));
                out.append(
                        Text.translatable(
                                "auraskills.granite.command.sources.line",
                                index,
                                s.getDisplayName(lo),
                                NumberUtil.format2(s.getXp()),
                                s.getId().toString(),
                                s.getType().getId().toString()));
            }
            MutableText message = out;
            source.sendFeedback(() -> message, false);
        }
    }

    /**
     * In-game: open the sources GUI, or on failure the chat list. **Console path:** {@code
     * menuPlayer} is null → chat to {@code feedbackSource} with {@code lo}.
     */
    public static int runGuiOrChatFor(
            FabricAuraSkills p,
            ServerCommandSource feedbackSource,
            ServerPlayerEntity menuPlayer,
            Locale lo,
            Skill skill,
            FabricSourceSortType sort,
            int returnToSkillsListPage) {
        LoadedSkill loaded = p.getSkillManager().getSkill(skill);
        List<XpSource> list = FabricSourcesListSupport.buildSorted(loaded, sort, lo);
        if (list.isEmpty()) {
            feedbackSource.sendFeedback(
                    () -> Text.translatable(
                            "auraskills.granite.command.sources.empty",
                            skill.getDisplayName(lo, false)),
                    false);
            return 1;
        }
        if (menuPlayer == null) {
            sendChatListing(feedbackSource, lo, skill, list, sort);
            return 1;
        }
        try {
            FabricAuraSkillsMenus.openSources(
                    skill, menuPlayer, p, sort, 0, returnToSkillsListPage);
        } catch (Throwable t) {
            p.logger().warn("Failed to open sources menu", t);
            feedbackSource.sendFeedback(
                    () -> Text.translatable("auraskills.granite.command.sources.menu_fallback"),
                    false);
            sendChatListing(feedbackSource, lo, skill, list, sort);
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
            FabricAuraSkills p,
            ServerCommandSource feedbackSource,
            ServerPlayerEntity menuPlayer,
            Locale lo,
            Skill skill,
            List<XpSource> list,
            FabricSourceSortType sort,
            int returnToSkillsListPage) {
        try {
            FabricAuraSkillsMenus.openSources(
                    skill, menuPlayer, p, sort, 0, returnToSkillsListPage);
            return true;
        } catch (Throwable t) {
            p.logger().warn("Failed to open sources menu", t);
            feedbackSource.sendFeedback(
                    () -> Text.translatable("auraskills.granite.command.sources.menu_fallback"),
                    false);
            sendChatListing(feedbackSource, lo, skill, list, sort);
            return false;
        }
    }
}
