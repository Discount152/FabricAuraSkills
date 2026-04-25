package dev.aurelium.auraskills.fabric.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.source.XpSource;
import dev.aurelium.auraskills.api.util.NumberUtil;
import dev.aurelium.auraskills.common.message.type.CommandMessage;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.common.skill.LoadedSkill;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static net.minecraft.server.command.CommandManager.literal;

/**
 * Bukkit {@code SkillsRootCommand#onSources} opens the Slate **sources** menu; **Sprint 57** — a
 * bounded **chat** listing of configured {@link XpSource}s for a skill (same sort modes as the
 * reference menu), using the player’s {@link User#getLocale()} for display names.
 */
public final class FabricSourcesCommands {

    private static final String ARG_SKILL = "skill";
    private static final String ARG_SORT = "sort";

    /** Same as {@code SourcesMenu.DEF_ITEMS_PER_PAGE} — chunk long chat output. */
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
        if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
            source.sendError(Text.translatable("auraskills.granite.command.sources.players_only"));
            return 0;
        }
        String skillRaw = StringArgumentType.getString(ctx, ARG_SKILL);
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            Skill skill = FabricCommandSkillParser.parse(p, skillRaw);
                            if (skill == null) {
                                User u = p.getUserManager().getUser(player.getUuid());
                                Locale loc =
                                        u != null ? u.getLocale() : p.getDefaultLanguage();
                                String body =
                                        p.getMessageProvider()
                                                .applyFormatting(
                                                        p.getMsg(CommandMessage.UNKNOWN_SKILL, loc));
                                source.sendError(Text.literal(body));
                                return 0;
                            }
                            User user = p.getUserManager().getUser(player.getUuid());
                            Locale lo =
                                    user != null
                                            ? user.getLocale()
                                            : p.getDefaultLanguage();
                            LoadedSkill loaded = p.getSkillManager().getSkill(skill);
                            List<XpSource> list = new ArrayList<>(loaded.sources());
                            if (list.isEmpty()) {
                                source.sendFeedback(
                                        () -> Text.translatable(
                                                "auraskills.granite.command.sources.empty",
                                                skill.getDisplayName(lo, false)),
                                        false);
                                return 1;
                            }
                            list.removeIf(s -> !s.isVersionValid());
                            Collections.sort(list, sort.comparator(lo));
                            sendInChunks(source, lo, skill, list, sort);
                            return 1;
                        })
                .orElseGet(
                        () -> {
                            source.sendError(Text.literal("AuraSkills is still starting."));
                            return 0;
                        });
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
