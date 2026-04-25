package dev.aurelium.auraskills.fabric.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.menu.FabricLeaderboardMenuGui;
import dev.aurelium.auraskills.fabric.menu.FabricMenuKeys;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.server.command.CommandManager.literal;

/**
 * {@code /openmenu leaderboard …} (**Sprint 72**). Bukkit {@code /top} is chat; the reference Slate
 * menu is opened from other screens with a {@code skill} property — this is a separate Granite
 * browser over the same {@link dev.aurelium.auraskills.common.leaderboard.LeaderboardManager} data.
 */
public final class FabricOpenMenuLeaderboard {

    private static final String ARG_SELF_POWER_PAGE = "lb_self_power_page";
    private static final String ARG_LB_TOKEN = "lb_token";
    private static final String ARG_LB_TOKEN_INT = "lb_token_int";
    private static final String ARG_LB_TARGET_SKILL = "lb_target_skill";
    private static final String ARG_LB_PAGE = "lb_page";
    private static final String LIT_AVERAGE = "average";

    private FabricOpenMenuLeaderboard() {}

    public static LiteralArgumentBuilder<ServerCommandSource> node() {
        return literal(FabricMenuKeys.LEADERBOARD)
                .executes(FabricOpenMenuLeaderboard::executeSelfPower1)
                .then(
                        literal(LIT_AVERAGE)
                                .executes(FabricOpenMenuLeaderboard::executeSelfAverage1)
                                .then(
                                        CommandManager.argument(
                                                        ARG_LB_PAGE, IntegerArgumentType.integer(1))
                                                .executes(
                                                        FabricOpenMenuLeaderboard
                                                                ::executeSelfAverageWithPageArg)))
                .then(
                        CommandManager.argument(
                                        ARG_SELF_POWER_PAGE, IntegerArgumentType.integer(1))
                                .executes(FabricOpenMenuLeaderboard::executeSelfPowerWithPage))
                .then(
                        CommandManager.argument(ARG_LB_TOKEN, StringArgumentType.word())
                                .suggests(
                                        FabricCommandSuggestionProviders
                                                .onlinePlayersAndEnabledSkillKeys())
                                .executes(FabricOpenMenuLeaderboard::executeToken)
                                .then(
                                        CommandManager.argument(
                                                        ARG_LB_TOKEN_INT,
                                                        IntegerArgumentType.integer(1))
                                                .executes(
                                                        FabricOpenMenuLeaderboard
                                                                ::executeTokenPlusInt))
                                .then(
                                        literal(LIT_AVERAGE)
                                                .executes(
                                                        FabricOpenMenuLeaderboard
                                                                ::executeTokenAverageNoPage)
                                                .then(
                                                        CommandManager.argument(
                                                                        ARG_LB_PAGE,
                                                                        IntegerArgumentType
                                                                                .integer(1))
                                                                .executes(
                                                                        FabricOpenMenuLeaderboard
                                                                                ::executeTokenAverageWithPageArg)))
                                .then(
                                        CommandManager.argument(
                                                        ARG_LB_TARGET_SKILL,
                                                        StringArgumentType.word())
                                                .suggests(
                                                        FabricCommandSuggestionProviders
                                                                .enabledSkillKeys())
                                                .executes(
                                                        FabricOpenMenuLeaderboard
                                                                ::executeTokenTargetSkill1)
                                                .then(
                                                        CommandManager.argument(
                                                                        ARG_LB_PAGE,
                                                                        IntegerArgumentType
                                                                                .integer(1))
                                                                .executes(
                                                                        FabricOpenMenuLeaderboard
                                                                                ::executeTokenTargetSkillWithPage))));
    }

    private static int executeSelfPower1(CommandContext<ServerCommandSource> ctx) {
        return forExecutorPlayer(
                ctx,
                (p, s, pl) -> {
                    FabricLeaderboardMenuGui.open(pl, p, FabricLeaderboardMenuGui.Mode.POWER, null, 1);
                    return 1;
                });
    }

    private static int executeSelfPowerWithPage(CommandContext<ServerCommandSource> ctx) {
        int page = IntegerArgumentType.getInteger(ctx, ARG_SELF_POWER_PAGE);
        return forExecutorPlayer(
                ctx,
                (p, s, pl) -> {
                    FabricLeaderboardMenuGui.open(pl, p, FabricLeaderboardMenuGui.Mode.POWER, null, page);
                    return 1;
                });
    }

    private static int executeSelfAverage1(CommandContext<ServerCommandSource> ctx) {
        return forExecutorPlayer(
                ctx,
                (p, s, pl) -> {
                    FabricLeaderboardMenuGui.open(
                            pl, p, FabricLeaderboardMenuGui.Mode.AVERAGE, null, 1);
                    return 1;
                });
    }

    private static int executeSelfAverageWithPageArg(CommandContext<ServerCommandSource> ctx) {
        int page = IntegerArgumentType.getInteger(ctx, ARG_LB_PAGE);
        return forExecutorPlayer(
                ctx,
                (p, s, pl) -> {
                    FabricLeaderboardMenuGui.open(
                            pl, p, FabricLeaderboardMenuGui.Mode.AVERAGE, null, page);
                    return 1;
                });
    }

    private static int executeToken(CommandContext<ServerCommandSource> ctx) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            String token = StringArgumentType.getString(ctx, ARG_LB_TOKEN);
                            var online =
                                    FabricCommandPlayerTarget.findOnline(
                                            ctx.getSource().getServer(), token);
                            if (online.isPresent()) {
                                return openForMenuRecipient(
                                        ctx,
                                        p,
                                        online.get(),
                                        FabricLeaderboardMenuGui.Mode.POWER,
                                        null,
                                        1);
                            }
                            Skill sk = FabricCommandSkillParser.parse(p, token);
                            if (sk == null) {
                                ctx.getSource()
                                        .sendError(
                                                Text.translatable(
                                                        "auraskills.granite.command.openmenu"
                                                                + ".leaderboard_bad_token",
                                                        token));
                                return 0;
                            }
                            ServerCommandSource s = ctx.getSource();
                            if (!(s.getEntity() instanceof ServerPlayerEntity pl)) {
                                s.sendError(
                                        Text.translatable(
                                                "auraskills.granite.command.openmenu"
                                                        + ".leaderboard_console"));
                                return 0;
                            }
                            FabricLeaderboardMenuGui.open(
                                    pl, p, FabricLeaderboardMenuGui.Mode.SKILL, sk, 1);
                            s.sendFeedback(
                                    () -> Text.translatable("auraskills.granite.menu.leaderboard_opened"),
                                    true);
                            return 1;
                        })
                .orElseGet(
                        () -> {
                            ctx.getSource()
                                    .sendError(Text.literal("AuraSkills is still starting."));
                            return 0;
                        });
    }

    private static int executeTokenPlusInt(CommandContext<ServerCommandSource> ctx) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            String token = StringArgumentType.getString(ctx, ARG_LB_TOKEN);
                            int n = IntegerArgumentType.getInteger(ctx, ARG_LB_TOKEN_INT);
                            var online =
                                    FabricCommandPlayerTarget.findOnline(
                                            ctx.getSource().getServer(), token);
                            if (online.isPresent()) {
                                return openForMenuRecipient(
                                        ctx,
                                        p,
                                        online.get(),
                                        FabricLeaderboardMenuGui.Mode.POWER,
                                        null,
                                        n);
                            }
                            Skill sk = FabricCommandSkillParser.parse(p, token);
                            if (sk == null) {
                                ctx.getSource()
                                        .sendError(
                                                Text.translatable(
                                                        "auraskills.granite.command.openmenu"
                                                                + ".leaderboard_bad_token",
                                                        token));
                                return 0;
                            }
                            ServerCommandSource s = ctx.getSource();
                            if (!(s.getEntity() instanceof ServerPlayerEntity pl)) {
                                s.sendError(
                                        Text.translatable(
                                                "auraskills.granite.command.openmenu"
                                                        + ".leaderboard_console"));
                                return 0;
                            }
                            FabricLeaderboardMenuGui.open(
                                    pl, p, FabricLeaderboardMenuGui.Mode.SKILL, sk, n);
                            s.sendFeedback(
                                    () -> Text.translatable("auraskills.granite.menu.leaderboard_opened"),
                                    true);
                            return 1;
                        })
                .orElseGet(
                        () -> {
                            ctx.getSource()
                                    .sendError(Text.literal("AuraSkills is still starting."));
                            return 0;
                        });
    }

    private static int executeTokenAverageNoPage(CommandContext<ServerCommandSource> ctx) {
        return tokenAveragePage(ctx, 1);
    }

    private static int executeTokenAverageWithPageArg(CommandContext<ServerCommandSource> ctx) {
        return tokenAveragePage(
                ctx, IntegerArgumentType.getInteger(ctx, ARG_LB_PAGE));
    }

    private static int tokenAveragePage(CommandContext<ServerCommandSource> ctx, int page) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            String tName = StringArgumentType.getString(ctx, ARG_LB_TOKEN);
                            var o = FabricCommandPlayerTarget.findOnline(ctx.getSource().getServer(), tName);
                            if (o.isEmpty()) {
                                ctx.getSource()
                                        .sendError(
                                                Text.translatable(
                                                        "auraskills.granite.command.xp.player_not_found",
                                                        tName));
                                return 0;
                            }
                            return openForMenuRecipient(
                                    ctx, p, o.get(), FabricLeaderboardMenuGui.Mode.AVERAGE, null, page);
                        })
                .orElseGet(
                        () -> {
                            ctx.getSource()
                                    .sendError(Text.literal("AuraSkills is still starting."));
                            return 0;
                        });
    }

    private static int executeTokenTargetSkill1(CommandContext<ServerCommandSource> ctx) {
        return tokenTargetSkill(
                ctx, 1);
    }

    private static int executeTokenTargetSkillWithPage(CommandContext<ServerCommandSource> ctx) {
        return tokenTargetSkill(
                ctx, IntegerArgumentType.getInteger(ctx, ARG_LB_PAGE));
    }

    private static int tokenTargetSkill(CommandContext<ServerCommandSource> ctx, int page) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            String tName = StringArgumentType.getString(ctx, ARG_LB_TOKEN);
                            String raw = StringArgumentType.getString(ctx, ARG_LB_TARGET_SKILL);
                            var o = FabricCommandPlayerTarget.findOnline(ctx.getSource().getServer(), tName);
                            if (o.isEmpty()) {
                                ctx.getSource()
                                        .sendError(
                                                Text.translatable(
                                                        "auraskills.granite.command.xp.player_not_found",
                                                        tName));
                                return 0;
                            }
                            Skill sk = FabricCommandSkillParser.parse(p, raw);
                            if (sk == null) {
                                ctx.getSource()
                                        .sendError(
                                                Text.translatable(
                                                        "auraskills.granite.command.openmenu.unknown_skill",
                                                        raw));
                                return 0;
                            }
                            return openForMenuRecipient(
                                    ctx, p, o.get(), FabricLeaderboardMenuGui.Mode.SKILL, sk, page);
                        })
                .orElseGet(
                        () -> {
                            ctx.getSource()
                                    .sendError(Text.literal("AuraSkills is still starting."));
                            return 0;
                        });
    }

    @FunctionalInterface
    private interface ExecutorPlayerAction {
        int act(FabricAuraSkills p, ServerCommandSource s, ServerPlayerEntity executorPlayer);
    }

    private static int forExecutorPlayer(
            CommandContext<ServerCommandSource> ctx, ExecutorPlayerAction act) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            ServerCommandSource s = ctx.getSource();
                            if (!(s.getEntity() instanceof ServerPlayerEntity pl)) {
                                s.sendError(
                                        Text.translatable(
                                                "auraskills.granite.command.openmenu"
                                                        + ".leaderboard_console"));
                                return 0;
                            }
                            int r = act.act(p, s, pl);
                            if (r == 1) {
                                s.sendFeedback(
                                        () -> Text.translatable("auraskills.granite.menu.leaderboard_opened"),
                                        true);
                            }
                            return r;
                        })
                .orElseGet(
                        () -> {
                            ctx.getSource()
                                    .sendError(Text.literal("AuraSkills is still starting."));
                            return 0;
                        });
    }

    private static int openForMenuRecipient(
            CommandContext<ServerCommandSource> ctx,
            FabricAuraSkills p,
            ServerPlayerEntity menuRecipient,
            FabricLeaderboardMenuGui.Mode mode,
            @Nullable Skill skill,
            int page) {
        FabricLeaderboardMenuGui.open(menuRecipient, p, mode, skill, page);
        ServerCommandSource s = ctx.getSource();
        if (s.getEntity() instanceof ServerPlayerEntity ex
                && ex.getUuid().equals(menuRecipient.getUuid())) {
            s.sendFeedback(
                    () -> Text.translatable("auraskills.granite.menu.leaderboard_opened"), true);
        } else {
            s.sendFeedback(
                    () -> Text.translatable(
                            "auraskills.granite.command.openmenu.opened_for",
                            FabricMenuKeys.LEADERBOARD,
                            menuRecipient.getName().getString()),
                    true);
        }
        return 1;
    }
}
