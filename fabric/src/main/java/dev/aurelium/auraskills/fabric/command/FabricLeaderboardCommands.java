package dev.aurelium.auraskills.fabric.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.util.NumberUtil;
import dev.aurelium.auraskills.common.leaderboard.LeaderboardManager;
import dev.aurelium.auraskills.common.leaderboard.SkillValue;
import dev.aurelium.auraskills.common.message.type.CommandMessage;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.common.util.text.TextUtil;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.text.FabricGraniteText;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static net.minecraft.server.command.CommandManager.literal;

/**
 * Top / skill / rank commands (**Sprint 47**). **Sprint 49:** {@code /top average [page]},
 * {@code /updateleaderboards [samples]}. Uses {@link LeaderboardManager} (same as Bukkit) and
 * {@link CommandMessage} keys. Bukkit tangles {@code top}/{@code skilltop} and {@code rank}/{@code
 * skillrank} as <strong>aliases</strong> of the same subcommands; Granite exposes
 * {@code /top}, {@code /skilltop <skill>}, {@code /rank}, {@code /skillrank <skill>} as separate
 * roots and also under {@code /auraskills} and {@code /sk|skill} via shared {@link
 * LiteralArgumentBuilder} factories.
 */
public final class FabricLeaderboardCommands {

    private static final String ARG_PAGE = "page";
    private static final String ARG_SKILL = "skill";
    private static final String ARG_SAMPLES = "samples";
    private static final String LIT_AVERAGE = "average";

    private FabricLeaderboardCommands() {}

    public static LiteralArgumentBuilder<ServerCommandSource> topRoot() {
        return literal("top")
                .requires(FabricCommandRequirements.permission(FabricCommandPermissions.TOP, true))
                .executes(ctx -> executeTopPower(ctx, 1))
                .then(
                        literal(LIT_AVERAGE)
                                .executes(ctx -> executeTopAverage(ctx, 1))
                                .then(
                                        CommandManager.argument(
                                                        ARG_PAGE, IntegerArgumentType.integer(1))
                                                .executes(
                                                        ctx ->
                                                                executeTopAverage(
                                                                        ctx,
                                                                        IntegerArgumentType
                                                                                .getInteger(
                                                                                        ctx,
                                                                                        ARG_PAGE)))))
                .then(
                        CommandManager.argument(ARG_PAGE, IntegerArgumentType.integer(1))
                                .executes(
                                        ctx ->
                                                executeTopPower(
                                                        ctx,
                                                        IntegerArgumentType.getInteger(
                                                                ctx, ARG_PAGE))));
    }

    public static LiteralArgumentBuilder<ServerCommandSource> updateLeaderboardsRoot() {
        return literal("updateleaderboards")
                .requires(
                        FabricCommandRequirements.permission(
                                FabricCommandPermissions.UPDATE_LEADERBOARDS, false))
                .executes(ctx -> executeUpdateLeaderboards(ctx, 1))
                .then(
                        CommandManager.argument(
                                        ARG_SAMPLES, IntegerArgumentType.integer(1, 64))
                                .executes(
                                        ctx ->
                                                executeUpdateLeaderboards(
                                                        ctx,
                                                        IntegerArgumentType.getInteger(
                                                                ctx, ARG_SAMPLES))));
    }

    public static LiteralArgumentBuilder<ServerCommandSource> skillTopRoot() {
        return literal("skilltop")
                .requires(
                        FabricCommandRequirements.permission(
                                FabricCommandPermissions.TOP, true))
                .then(
                        CommandManager.argument(ARG_SKILL, StringArgumentType.word())
                                .suggests(FabricCommandSuggestionProviders.enabledSkillKeys())
                                .executes(FabricLeaderboardCommands::executeSkillTopPage1)
                                .then(
                                        CommandManager.argument(
                                                        ARG_PAGE, IntegerArgumentType.integer(1))
                                                .executes(
                                                        FabricLeaderboardCommands
                                                                ::executeSkillTopWithPage)));
    }

    public static LiteralArgumentBuilder<ServerCommandSource> rankRoot() {
        return literal("rank")
                .requires(
                        FabricCommandRequirements.permission(
                                FabricCommandPermissions.RANK, true))
                .executes(FabricLeaderboardCommands::executeRank);
    }

    public static LiteralArgumentBuilder<ServerCommandSource> skillRankRoot() {
        return literal("skillrank")
                .requires(
                        FabricCommandRequirements.permission(
                                FabricCommandPermissions.RANK, true))
                .then(
                        CommandManager.argument(ARG_SKILL, StringArgumentType.word())
                                .suggests(FabricCommandSuggestionProviders.enabledSkillKeys())
                                .executes(FabricLeaderboardCommands::executeSkillRank));
    }

    private static int executeTopPower(CommandContext<ServerCommandSource> ctx, int page) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            ServerCommandSource source = ctx.getSource();
                            LeaderboardManager lb = p.getLeaderboardManager();
                            List<SkillValue> list =
                                    lb.getPowerLeaderboard(
                                            page, FabricCommandLeaderboardText.ENTRIES_PER_PAGE);
                            Locale lo = localeForSource(source, p);
                            if (page <= 1) {
                                source.sendFeedback(
                                        () -> Text.literal(
                                                p.getMsg(CommandMessage.TOP_POWER_HEADER, lo)),
                                        false);
                            } else {
                                String header =
                                        p.getMsg(CommandMessage.TOP_POWER_HEADER_PAGE, lo)
                                                .replace("{page}", String.valueOf(page));
                                source.sendFeedback(() -> Text.literal(header), false);
                            }
                            for (int i = 0; i < list.size(); i++) {
                                SkillValue sv = list.get(i);
                                int rank =
                                        FabricCommandLeaderboardText.globalRank1Based(page, i);
                                String name =
                                        FabricCommandLeaderboardText.resolvePlayerName(
                                                source.getServer(), sv.id());
                                String line =
                                        p.getMsg(CommandMessage.TOP_POWER_ENTRY, lo)
                                                .replace("{rank}", String.valueOf(rank))
                                                .replace("{player}", name)
                                                .replace(
                                                        "{level}", String.valueOf(sv.level()));
                                final String out = line;
                                source.sendFeedback(() -> Text.literal(out), false);
                            }
                            return 1;
                        })
                .orElseGet(
                        () -> {
                            ctx.getSource()
                                    .sendError(Text.literal("AuraSkills is still starting."));
                            return 0;
                        });
    }

    private static int executeTopAverage(CommandContext<ServerCommandSource> ctx, int page) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            ServerCommandSource source = ctx.getSource();
                            LeaderboardManager lb = p.getLeaderboardManager();
                            List<SkillValue> list =
                                    lb.getAverageLeaderboard(
                                            page, FabricCommandLeaderboardText.ENTRIES_PER_PAGE);
                            Locale lo = localeForSource(source, p);
                            if (page <= 1) {
                                source.sendFeedback(
                                        () -> Text.literal(
                                                p.getMsg(CommandMessage.TOP_AVERAGE_HEADER, lo)),
                                        false);
                            } else {
                                String header =
                                        p.getMsg(CommandMessage.TOP_AVERAGE_HEADER_PAGE, lo)
                                                .replace("{page}", String.valueOf(page));
                                source.sendFeedback(() -> Text.literal(header), false);
                            }
                            for (int i = 0; i < list.size(); i++) {
                                SkillValue sv = list.get(i);
                                int rank =
                                        FabricCommandLeaderboardText.globalRank1Based(page, i);
                                String name =
                                        FabricCommandLeaderboardText.resolvePlayerName(
                                                source.getServer(), sv.id());
                                String line =
                                        TextUtil.replace(
                                                p.getMsg(CommandMessage.TOP_AVERAGE_ENTRY, lo),
                                                "{rank}",
                                                String.valueOf(rank),
                                                "{player}",
                                                name,
                                                "{level}",
                                                NumberUtil.format2(sv.xp()));
                                final String out = line;
                                source.sendFeedback(() -> Text.literal(out), false);
                            }
                            return 1;
                        })
                .orElseGet(
                        () -> {
                            ctx.getSource()
                                    .sendError(Text.literal("AuraSkills is still starting."));
                            return 0;
                        });
    }

    private static int executeUpdateLeaderboards(CommandContext<ServerCommandSource> ctx, int samples) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            ServerCommandSource source = ctx.getSource();
                            LeaderboardManager lb = p.getLeaderboardManager();
                            Locale lo = localeForSource(source, p);
                            if (!lb.isNotSorting()) {
                                source.sendError(
                                        Text.literal(
                                                p.getMsg(
                                                        CommandMessage
                                                                .UPDATELEADERBOARDS_ALREADY_UPDATING,
                                                        lo)));
                                return 0;
                            }
                            p.getScheduler()
                                    .executeAsync(
                                            () -> {
                                                long sum = 0L;
                                                for (int i = 0; i < samples; i++) {
                                                    sum += lb.updateLeaderboards();
                                                }
                                                double avg = sum / (double) samples;
                                                p.getScheduler()
                                                        .executeSync(
                                                                () -> {
                                                                    source.sendFeedback(
                                                                            () ->
                                                                                    Text.literal(
                                                                                            p.getMsg(
                                                                                                    CommandMessage
                                                                                                            .UPDATELEADERBOARDS_UPDATED,
                                                                                                    lo)),
                                                                            false);
                                                                    if (samples > 1) {
                                                                        source.sendFeedback(
                                                                                () ->
                                                                                        FabricGraniteText.key(
                                                                                                "auraskills.granite.command.updateleaderboards.samples",
                                                                                                samples,
                                                                                                Math.round(
                                                                                                        avg)),
                                                                                false);
                                                                    }
                                                                });
                                            });
                            return 1;
                        })
                .orElseGet(
                        () -> {
                            ctx.getSource()
                                    .sendError(Text.literal("AuraSkills is still starting."));
                            return 0;
                        });
    }

    private static int executeSkillTopPage1(CommandContext<ServerCommandSource> ctx) {
        return runSkillTop(ctx, StringArgumentType.getString(ctx, ARG_SKILL), 1);
    }

    private static int executeSkillTopWithPage(CommandContext<ServerCommandSource> ctx) {
        return runSkillTop(
                ctx,
                StringArgumentType.getString(ctx, ARG_SKILL),
                IntegerArgumentType.getInteger(ctx, ARG_PAGE));
    }

    private static int runSkillTop(
            CommandContext<ServerCommandSource> ctx, String rawSkill, int page) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            ServerCommandSource source = ctx.getSource();
                            Skill skill = FabricCommandSkillParser.parse(p, rawSkill);
                            if (skill == null) {
                                unknownSkillOrUsage(source, p);
                                return 0;
                            }
                            LeaderboardManager lb = p.getLeaderboardManager();
                            List<SkillValue> list =
                                    lb.getLeaderboard(
                                            skill,
                                            page,
                                            FabricCommandLeaderboardText.ENTRIES_PER_PAGE);
                            Locale lo = localeForSource(source, p);
                            if (page <= 1) {
                                String h =
                                        p.getMsg(CommandMessage.TOP_SKILL_HEADER, lo)
                                                .replace(
                                                        "{skill}",
                                                        skill.getDisplayName(lo));
                                source.sendFeedback(() -> Text.literal(h), false);
                            } else {
                                String h =
                                        p.getMsg(CommandMessage.TOP_SKILL_HEADER_PAGE, lo)
                                                .replace(
                                                        "{skill}",
                                                        skill.getDisplayName(lo))
                                                .replace("{page}", String.valueOf(page));
                                source.sendFeedback(() -> Text.literal(h), false);
                            }
                            for (int i = 0; i < list.size(); i++) {
                                SkillValue sv = list.get(i);
                                int rank =
                                        FabricCommandLeaderboardText.globalRank1Based(page, i);
                                String name =
                                        FabricCommandLeaderboardText.resolvePlayerName(
                                                source.getServer(), sv.id());
                                String line =
                                        p.getMsg(CommandMessage.TOP_SKILL_ENTRY, lo)
                                                .replace("{rank}", String.valueOf(rank))
                                                .replace("{player}", name)
                                                .replace(
                                                        "{level}", String.valueOf(sv.level()));
                                final String out = line;
                                source.sendFeedback(() -> Text.literal(out), false);
                            }
                            return 1;
                        })
                .orElseGet(
                        () -> {
                            ctx.getSource()
                                    .sendError(Text.literal("AuraSkills is still starting."));
                            return 0;
                        });
    }

    private static int executeRank(CommandContext<ServerCommandSource> ctx) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            ServerCommandSource source = ctx.getSource();
                            if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
                                source.sendError(
                                        FabricGraniteText.key(
                                                "auraskills.granite.command.leaderboard.players_only"));
                                return 0;
                            }
                            User user = p.getUserManager().getUser(player.getUuid());
                            if (user == null) {
                                source.sendError(
                                        Text.literal(
                                                p.getMsg(
                                                        CommandMessage.NO_PROFILE,
                                                        p.getDefaultLanguage())));
                                return 0;
                            }
                            LeaderboardManager lb = p.getLeaderboardManager();
                            Locale lo = user.getLocale();
                            UUID self = player.getUuid();
                            int powerRank = lb.getPowerRank(self);
                            int powerTotal = lb.getPowerLeaderboard().size();
                            source.sendFeedback(
                                    () -> Text.literal(
                                            p.getMsg(CommandMessage.RANK_HEADER, lo)),
                                    false);
                            String powerLine =
                                    p.getMsg(CommandMessage.RANK_POWER, lo)
                                            .replace("{rank}", String.valueOf(powerRank))
                                            .replace("{total}", String.valueOf(powerTotal));
                            source.sendFeedback(() -> Text.literal(powerLine), false);
                            for (Skill sk : p.getSkillManager().getEnabledSkills()) {
                                int r = lb.getSkillRank(sk, self);
                                int t = lb.getLeaderboard(sk).size();
                                String line =
                                        p.getMsg(CommandMessage.RANK_ENTRY, lo)
                                                .replace(
                                                        "{skill}", sk.getDisplayName(lo))
                                                .replace("{rank}", String.valueOf(r))
                                                .replace("{total}", String.valueOf(t));
                                final String out = line;
                                source.sendFeedback(() -> Text.literal(out), false);
                            }
                            return 1;
                        })
                .orElseGet(
                        () -> {
                            ctx.getSource()
                                    .sendError(Text.literal("AuraSkills is still starting."));
                            return 0;
                        });
    }

    private static int executeSkillRank(CommandContext<ServerCommandSource> ctx) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            ServerCommandSource source = ctx.getSource();
                            if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
                                source.sendError(
                                        FabricGraniteText.key(
                                                "auraskills.granite.command.leaderboard.players_only"));
                                return 0;
                            }
                            String raw = StringArgumentType.getString(ctx, ARG_SKILL);
                            Skill skill = FabricCommandSkillParser.parse(p, raw);
                            if (skill == null) {
                                unknownSkillOrUsage(source, p);
                                return 0;
                            }
                            User user = p.getUserManager().getUser(player.getUuid());
                            if (user == null) {
                                source.sendError(
                                        Text.literal(
                                                p.getMsg(
                                                        CommandMessage.NO_PROFILE,
                                                        p.getDefaultLanguage())));
                                return 0;
                            }
                            LeaderboardManager lb = p.getLeaderboardManager();
                            Locale lo = user.getLocale();
                            int r = lb.getSkillRank(skill, player.getUuid());
                            int t = lb.getLeaderboard(skill).size();
                            String line =
                                    p.getMsg(CommandMessage.RANK_ENTRY, lo)
                                            .replace(
                                                    "{skill}", skill.getDisplayName(lo))
                                            .replace("{rank}", String.valueOf(r))
                                            .replace("{total}", String.valueOf(t));
                            source.sendFeedback(() -> Text.literal(line), false);
                            return 1;
                        })
                .orElseGet(
                        () -> {
                            ctx.getSource()
                                    .sendError(Text.literal("AuraSkills is still starting."));
                            return 0;
                        });
    }

    private static void unknownSkillOrUsage(ServerCommandSource source, FabricAuraSkills p) {
        source.sendError(
                Text.literal(p.getMsg(CommandMessage.TOP_USAGE, p.getDefaultLanguage())));
    }

    private static Locale localeForSource(ServerCommandSource source, FabricAuraSkills p) {
        if (source.getEntity() instanceof ServerPlayerEntity sp) {
            User u = p.getUserManager().getUser(sp.getUuid());
            if (u != null) {
                return u.getLocale();
            }
        }
        return p.getDefaultLanguage();
    }
}
