package dev.granite.graniteskills.fabric.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.api.util.NumberUtil;
import dev.granite.graniteskills.common.leaderboard.LeaderboardManager;
import dev.granite.graniteskills.common.leaderboard.SkillValue;
import dev.granite.graniteskills.common.message.type.CommandMessage;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.common.util.text.TextUtil;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import dev.granite.graniteskills.fabric.text.FabricGraniteText;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static net.minecraft.commands.Commands.literal;

/**
 * Top / skill / rank commands (**Sprint 47**). **Sprint 49:** {@code /top average [page]},
 * {@code /updateleaderboards [samples]}. Uses {@link LeaderboardManager} (same as Bukkit) and
 * {@link CommandMessage} keys. Bukkit tangles {@code top}/{@code skilltop} and {@code rank}/{@code
 * skillrank} as <strong>aliases</strong> of the same subcommands; Granite exposes
 * {@code /top}, {@code /skilltop <skill>}, {@code /rank}, {@code /skillrank <skill>} as separate
 * roots and also under {@code /graniteskills} and {@code /sk|skill} via shared {@link
 * LiteralArgumentBuilder} factories.
 */
public final class FabricLeaderboardCommands {

    private static final String ARG_PAGE = "page";
    private static final String ARG_SKILL = "skill";
    private static final String ARG_SAMPLES = "samples";
    private static final String LIT_AVERAGE = "average";

    private FabricLeaderboardCommands() {}

    public static LiteralArgumentBuilder<CommandSourceStack> topRoot() {
        return literal("top")
                .requires(FabricCommandRequirements.permission(FabricCommandPermissions.TOP, true))
                .executes(ctx -> executeTopPower(ctx, 1))
                .then(
                        literal(LIT_AVERAGE)
                                .executes(ctx -> executeTopAverage(ctx, 1))
                                .then(
                                        Commands.argument(
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
                        Commands.argument(ARG_PAGE, IntegerArgumentType.integer(1))
                                .executes(
                                        ctx ->
                                                executeTopPower(
                                                        ctx,
                                                        IntegerArgumentType.getInteger(
                                                                ctx, ARG_PAGE))));
    }

    public static LiteralArgumentBuilder<CommandSourceStack> updateLeaderboardsRoot() {
        return literal("updateleaderboards")
                .requires(
                        FabricCommandRequirements.permission(
                                FabricCommandPermissions.UPDATE_LEADERBOARDS, false))
                .executes(ctx -> executeUpdateLeaderboards(ctx, 1))
                .then(
                        Commands.argument(
                                        ARG_SAMPLES, IntegerArgumentType.integer(1, 64))
                                .executes(
                                        ctx ->
                                                executeUpdateLeaderboards(
                                                        ctx,
                                                        IntegerArgumentType.getInteger(
                                                                ctx, ARG_SAMPLES))));
    }

    public static LiteralArgumentBuilder<CommandSourceStack> skillTopRoot() {
        return literal("skilltop")
                .requires(
                        FabricCommandRequirements.permission(
                                FabricCommandPermissions.TOP, true))
                .then(
                        Commands.argument(ARG_SKILL, StringArgumentType.word())
                                .suggests(FabricCommandSuggestionProviders.enabledSkillKeys())
                                .executes(FabricLeaderboardCommands::executeSkillTopPage1)
                                .then(
                                        Commands.argument(
                                                        ARG_PAGE, IntegerArgumentType.integer(1))
                                                .executes(
                                                        FabricLeaderboardCommands
                                                                ::executeSkillTopWithPage)));
    }

    public static LiteralArgumentBuilder<CommandSourceStack> rankRoot() {
        return literal("rank")
                .requires(
                        FabricCommandRequirements.permission(
                                FabricCommandPermissions.RANK, true))
                .executes(FabricLeaderboardCommands::executeRank);
    }

    public static LiteralArgumentBuilder<CommandSourceStack> skillRankRoot() {
        return literal("skillrank")
                .requires(
                        FabricCommandRequirements.permission(
                                FabricCommandPermissions.RANK, true))
                .then(
                        Commands.argument(ARG_SKILL, StringArgumentType.word())
                                .suggests(FabricCommandSuggestionProviders.enabledSkillKeys())
                                .executes(FabricLeaderboardCommands::executeSkillRank));
    }

    private static int executeTopPower(CommandContext<CommandSourceStack> ctx, int page) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            CommandSourceStack source = ctx.getSource();
                            LeaderboardManager lb = p.getLeaderboardManager();
                            List<SkillValue> list =
                                    lb.getPowerLeaderboard(
                                            page, FabricCommandLeaderboardText.ENTRIES_PER_PAGE);
                            Locale lo = localeForSource(source, p);
                            if (page <= 1) {
                                source.sendSuccess(
                                        () -> Component.literal(
                                                p.getMsg(CommandMessage.TOP_POWER_HEADER, lo)),
                                        false);
                            } else {
                                String header =
                                        p.getMsg(CommandMessage.TOP_POWER_HEADER_PAGE, lo)
                                                .replace("{page}", String.valueOf(page));
                                source.sendSuccess(() -> Component.literal(header), false);
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
                                source.sendSuccess(() -> Component.literal(out), false);
                            }
                            return 1;
                        })
                .orElseGet(
                        () -> {
                            ctx.getSource()
                                    .sendFailure(Component.literal("GraniteSkills is still starting."));
                            return 0;
                        });
    }

    private static int executeTopAverage(CommandContext<CommandSourceStack> ctx, int page) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            CommandSourceStack source = ctx.getSource();
                            LeaderboardManager lb = p.getLeaderboardManager();
                            List<SkillValue> list =
                                    lb.getAverageLeaderboard(
                                            page, FabricCommandLeaderboardText.ENTRIES_PER_PAGE);
                            Locale lo = localeForSource(source, p);
                            if (page <= 1) {
                                source.sendSuccess(
                                        () -> Component.literal(
                                                p.getMsg(CommandMessage.TOP_AVERAGE_HEADER, lo)),
                                        false);
                            } else {
                                String header =
                                        p.getMsg(CommandMessage.TOP_AVERAGE_HEADER_PAGE, lo)
                                                .replace("{page}", String.valueOf(page));
                                source.sendSuccess(() -> Component.literal(header), false);
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
                                source.sendSuccess(() -> Component.literal(out), false);
                            }
                            return 1;
                        })
                .orElseGet(
                        () -> {
                            ctx.getSource()
                                    .sendFailure(Component.literal("GraniteSkills is still starting."));
                            return 0;
                        });
    }

    private static int executeUpdateLeaderboards(CommandContext<CommandSourceStack> ctx, int samples) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            CommandSourceStack source = ctx.getSource();
                            LeaderboardManager lb = p.getLeaderboardManager();
                            Locale lo = localeForSource(source, p);
                            if (!lb.isNotSorting()) {
                                source.sendFailure(
                                        Component.literal(
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
                                                                    source.sendSuccess(
                                                                            () ->
                                                                                    Component.literal(
                                                                                            p.getMsg(
                                                                                                    CommandMessage
                                                                                                            .UPDATELEADERBOARDS_UPDATED,
                                                                                                    lo)),
                                                                            false);
                                                                    if (samples > 1) {
                                                                        source.sendSuccess(
                                                                                () ->
                                                                                        FabricGraniteText.key(
                                                                                                "graniteskills.granite.command.updateleaderboards.samples",
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
                                    .sendFailure(Component.literal("GraniteSkills is still starting."));
                            return 0;
                        });
    }

    private static int executeSkillTopPage1(CommandContext<CommandSourceStack> ctx) {
        return runSkillTop(ctx, StringArgumentType.getString(ctx, ARG_SKILL), 1);
    }

    private static int executeSkillTopWithPage(CommandContext<CommandSourceStack> ctx) {
        return runSkillTop(
                ctx,
                StringArgumentType.getString(ctx, ARG_SKILL),
                IntegerArgumentType.getInteger(ctx, ARG_PAGE));
    }

    private static int runSkillTop(
            CommandContext<CommandSourceStack> ctx, String rawSkill, int page) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            CommandSourceStack source = ctx.getSource();
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
                                source.sendSuccess(() -> Component.literal(h), false);
                            } else {
                                String h =
                                        p.getMsg(CommandMessage.TOP_SKILL_HEADER_PAGE, lo)
                                                .replace(
                                                        "{skill}",
                                                        skill.getDisplayName(lo))
                                                .replace("{page}", String.valueOf(page));
                                source.sendSuccess(() -> Component.literal(h), false);
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
                                source.sendSuccess(() -> Component.literal(out), false);
                            }
                            return 1;
                        })
                .orElseGet(
                        () -> {
                            ctx.getSource()
                                    .sendFailure(Component.literal("GraniteSkills is still starting."));
                            return 0;
                        });
    }

    private static int executeRank(CommandContext<CommandSourceStack> ctx) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            CommandSourceStack source = ctx.getSource();
                            if (!(source.getEntity() instanceof ServerPlayer player)) {
                                source.sendFailure(
                                        FabricGraniteText.key(
                                                "graniteskills.granite.command.leaderboard.players_only"));
                                return 0;
                            }
                            User user = p.getUserManager().getUser(player.getUUID());
                            if (user == null) {
                                source.sendFailure(
                                        Component.literal(
                                                p.getMsg(
                                                        CommandMessage.NO_PROFILE,
                                                        p.getDefaultLanguage())));
                                return 0;
                            }
                            LeaderboardManager lb = p.getLeaderboardManager();
                            Locale lo = user.getLocale();
                            UUID self = player.getUUID();
                            int powerRank = lb.getPowerRank(self);
                            int powerTotal = lb.getPowerLeaderboard().size();
                            source.sendSuccess(
                                    () -> Component.literal(
                                            p.getMsg(CommandMessage.RANK_HEADER, lo)),
                                    false);
                            String powerLine =
                                    p.getMsg(CommandMessage.RANK_POWER, lo)
                                            .replace("{rank}", String.valueOf(powerRank))
                                            .replace("{total}", String.valueOf(powerTotal));
                            source.sendSuccess(() -> Component.literal(powerLine), false);
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
                                source.sendSuccess(() -> Component.literal(out), false);
                            }
                            return 1;
                        })
                .orElseGet(
                        () -> {
                            ctx.getSource()
                                    .sendFailure(Component.literal("GraniteSkills is still starting."));
                            return 0;
                        });
    }

    private static int executeSkillRank(CommandContext<CommandSourceStack> ctx) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            CommandSourceStack source = ctx.getSource();
                            if (!(source.getEntity() instanceof ServerPlayer player)) {
                                source.sendFailure(
                                        FabricGraniteText.key(
                                                "graniteskills.granite.command.leaderboard.players_only"));
                                return 0;
                            }
                            String raw = StringArgumentType.getString(ctx, ARG_SKILL);
                            Skill skill = FabricCommandSkillParser.parse(p, raw);
                            if (skill == null) {
                                unknownSkillOrUsage(source, p);
                                return 0;
                            }
                            User user = p.getUserManager().getUser(player.getUUID());
                            if (user == null) {
                                source.sendFailure(
                                        Component.literal(
                                                p.getMsg(
                                                        CommandMessage.NO_PROFILE,
                                                        p.getDefaultLanguage())));
                                return 0;
                            }
                            LeaderboardManager lb = p.getLeaderboardManager();
                            Locale lo = user.getLocale();
                            int r = lb.getSkillRank(skill, player.getUUID());
                            int t = lb.getLeaderboard(skill).size();
                            String line =
                                    p.getMsg(CommandMessage.RANK_ENTRY, lo)
                                            .replace(
                                                    "{skill}", skill.getDisplayName(lo))
                                            .replace("{rank}", String.valueOf(r))
                                            .replace("{total}", String.valueOf(t));
                            source.sendSuccess(() -> Component.literal(line), false);
                            return 1;
                        })
                .orElseGet(
                        () -> {
                            ctx.getSource()
                                    .sendFailure(Component.literal("GraniteSkills is still starting."));
                            return 0;
                        });
    }

    private static void unknownSkillOrUsage(CommandSourceStack source, FabricGraniteSkills p) {
        source.sendFailure(
                Component.literal(p.getMsg(CommandMessage.TOP_USAGE, p.getDefaultLanguage())));
    }

    private static Locale localeForSource(CommandSourceStack source, FabricGraniteSkills p) {
        if (source.getEntity() instanceof ServerPlayer sp) {
            User u = p.getUserManager().getUser(sp.getUUID());
            if (u != null) {
                return u.getLocale();
            }
        }
        return p.getDefaultLanguage();
    }
}
