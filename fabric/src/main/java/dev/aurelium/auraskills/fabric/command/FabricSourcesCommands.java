package dev.aurelium.auraskills.fabric.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.source.XpSource;
import dev.aurelium.auraskills.api.util.NumberUtil;
import dev.aurelium.auraskills.common.message.type.CommandMessage;
import dev.aurelium.auraskills.common.skill.LoadedSkill;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.menu.FabricAuraSkillsMenus;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

import static net.minecraft.server.command.CommandManager.literal;

/**
 * Bukkit {@code SkillsRootCommand#onSources} opens the Slate <strong>sources</strong> menu. **Sprint
 * 57** added a paged <strong>chat</strong> listing. **Sprint 69** — in-game players open a Fabric
 * 6×9 {@link dev.aurelium.auraskills.fabric.menu.FabricSourcesMenuGui}; console/RCON and fallback keep
 * the same chat listing.
 */
public final class FabricSourcesCommands {

    private static final String ARG_SKILL = "skill";
    private static final String ARG_SORT = "sort";

    /** Same as {@code SourcesMenu.DEF_ITEMS_PER_PAGE} / Bukkit {@code items_per_page: 28} — chat chunks. */
    private static final int LINES_PER_MESSAGE = 28;

    private FabricSourcesCommands() {}

    public static LiteralArgumentBuilder<ServerCommandSource> sourcesLiteral() {
        return literal("sources")
                .requires(
                        FabricCommandRequirements.permission(
                                FabricCommandPermissions.SOURCES, true))
                .then(
                        CommandManager.argument(ARG_SKILL, StringArgumentType.word())
                                .suggests(FabricCommandSuggestionProviders.enabledSkillKeys())
                                .executes(ctx -> run(ctx, FabricSourceSortType.ASCENDING))
                                .then(
                                        CommandManager.argument(
                                                        ARG_SORT, StringArgumentType.word())
                                                .suggests(
                                                        FabricCommandSuggestionProviders
                                                                .sourceSortTypes())
                                                .executes(
                                                        FabricSourcesCommands::runWithSortArg)));
    }

    private static int runWithSortArg(CommandContext<ServerCommandSource> ctx) {
        String sortRaw = StringArgumentType.getString(ctx, ARG_SORT);
        return FabricSourceSortType.fromTokenStrict(sortRaw)
                .map(s -> run(ctx, s))
                .orElseGet(
                        () -> {
                            ctx.getSource()
                                    .sendError(
                                            Text.translatable(
                                                    "auraskills.granite.command.sources.bad_sort",
                                                    sortRaw));
                            return 0;
                        });
    }

    private static int run(
            CommandContext<ServerCommandSource> ctx, FabricSourceSortType sort) {
        ServerCommandSource source = ctx.getSource();
        String skillRaw = StringArgumentType.getString(ctx, ARG_SKILL);
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            ServerPlayerEntity player =
                                    source.getEntity() instanceof ServerPlayerEntity sp
                                            ? sp
                                            : null;
                            Locale lo = localeFor(p, player);
                            Skill skill = FabricCommandSkillParser.parse(p, skillRaw);
                            if (skill == null) {
                                String body =
                                        p.getMessageProvider()
                                                .applyFormatting(
                                                        p.getMsg(CommandMessage.UNKNOWN_SKILL, lo));
                                source.sendError(Text.literal(body));
                                return 0;
                            }
                            LoadedSkill loaded = p.getSkillManager().getSkill(skill);
                            List<XpSource> list =
                                    FabricSourcesListSupport.buildSorted(loaded, sort, lo);
                            if (list.isEmpty()) {
                                source.sendFeedback(
                                        () -> Text.translatable(
                                                "auraskills.granite.command.sources.empty",
                                                skill.getDisplayName(lo, false)),
                                        false);
                                return 1;
                            }
                            if (player == null) {
                                sendInChunks(source, lo, skill, list, sort);
                                return 1;
                            }
                            try {
                                FabricAuraSkillsMenus.openSources(
                                        skill, player, p, sort, 0, 0);
                            } catch (Throwable t) {
                                p.logger().warn("Failed to open sources menu", t);
                                source.sendFeedback(
                                        () -> Text.translatable(
                                                "auraskills.granite.command.sources.menu_fallback"),
                                        false);
                                sendInChunks(source, lo, skill, list, sort);
                            }
                            return 1;
                        })
                .orElseGet(
                        () -> {
                            source.sendError(Text.literal("AuraSkills is still starting."));
                            return 0;
                        });
    }

    private static Locale localeFor(FabricAuraSkills p, @Nullable ServerPlayerEntity player) {
        if (player == null) {
            return p.getDefaultLanguage();
        }
        User user = p.getUserManager().getUser(player.getUuid());
        return user != null ? user.getLocale() : p.getDefaultLanguage();
    }

    private static void sendInChunks(
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

    private static String sortKey(FabricSourceSortType sort) {
        return sort.name().toLowerCase(Locale.ROOT);
    }
}
