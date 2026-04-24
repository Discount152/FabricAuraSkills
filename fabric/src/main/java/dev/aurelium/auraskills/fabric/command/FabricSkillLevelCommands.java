package dev.aurelium.auraskills.fabric.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.util.NumberUtil;
import dev.aurelium.auraskills.common.message.type.CommandMessage;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Locale;

import static net.minecraft.server.command.CommandManager.literal;

/**
 * Bounded **skill level** family (Sprints 42–43), aligned with Bukkit
 * {@link dev.aurelium.auraskills.bukkit.commands.SkillCommand} ({@code setlevel},
 * {@code addlevel}, {@code setall}, {@code reset}, plus a Fabric {@code remove}). Registered
 * as {@code /skilllevel …} and {@code /auraskills skilllevel …}.
 */
public final class FabricSkillLevelCommands {

    private static final String ARG_PLAYER = "player";
    private static final String ARG_SKILL = "skill";
    private static final String ARG_LEVEL = "level";
    private static final String ARG_LEVELS = "levels";

    private FabricSkillLevelCommands() {}

    public static LiteralArgumentBuilder<ServerCommandSource> skillLevelRoot() {
        return literal("skilllevel")
                .then(view())
                .then(add())
                .then(set())
                .then(remove())
                .then(setall())
                .then(reset());
    }

    private static LiteralArgumentBuilder<ServerCommandSource> view() {
        return literal("view")
                .requires(
                        FabricCommandRequirements.permission(
                                FabricCommandPermissions.SKILL_LEVEL_VIEW, true))
                .then(
                        CommandManager.argument(ARG_PLAYER, StringArgumentType.word())
                                .suggests(FabricCommandSuggestionProviders.onlinePlayers())
                                .then(
                                        CommandManager.argument(ARG_SKILL, StringArgumentType.word())
                                                .suggests(
                                                        FabricCommandSuggestionProviders
                                                                .enabledSkillKeys())
                                                .executes(FabricSkillLevelCommands::executeView)));
    }

    private static LiteralArgumentBuilder<ServerCommandSource> add() {
        return literal("add")
                .requires(
                        FabricCommandRequirements.permission(
                                FabricCommandPermissions.SKILL_SETLEVEL, false))
                .then(wrappedPlayerSkillInt(
                        IntegerArgumentType.integer(1), ARG_LEVELS, FabricSkillLevelCommands::executeAdd));
    }

    private static LiteralArgumentBuilder<ServerCommandSource> set() {
        return literal("set")
                .requires(
                        FabricCommandRequirements.permission(
                                FabricCommandPermissions.SKILL_SETLEVEL, false))
                .then(wrappedPlayerSkillInt(
                        IntegerArgumentType.integer(0),
                        ARG_LEVEL,
                        FabricSkillLevelCommands::executeSet));
    }

    private static LiteralArgumentBuilder<ServerCommandSource> remove() {
        return literal("remove")
                .requires(
                        FabricCommandRequirements.permission(
                                FabricCommandPermissions.SKILL_SETLEVEL, false))
                .then(wrappedPlayerSkillInt(
                        IntegerArgumentType.integer(1),
                        ARG_LEVELS,
                        FabricSkillLevelCommands::executeRemove));
    }

    private static LiteralArgumentBuilder<ServerCommandSource> setall() {
        return literal("setall")
                .requires(
                        FabricCommandRequirements.permission(
                                FabricCommandPermissions.SKILL_SETLEVEL, false))
                .then(
                        CommandManager.argument(ARG_PLAYER, StringArgumentType.word())
                                .suggests(FabricCommandSuggestionProviders.onlinePlayers())
                                .then(
                                        CommandManager.argument(
                                                        ARG_LEVEL, IntegerArgumentType.integer(0))
                                                .executes(FabricSkillLevelCommands::executeSetAll)));
    }

    private static LiteralArgumentBuilder<ServerCommandSource> reset() {
        return literal("reset")
                .requires(
                        FabricCommandRequirements.permission(
                                FabricCommandPermissions.SKILL_RESET, false))
                .then(
                        CommandManager.argument(ARG_PLAYER, StringArgumentType.word())
                                .suggests(FabricCommandSuggestionProviders.onlinePlayers())
                                .executes(FabricSkillLevelCommands::executeResetAll)
                                .then(
                                        CommandManager.argument(ARG_SKILL, StringArgumentType.word())
                                                .suggests(
                                                        FabricCommandSuggestionProviders
                                                                .enabledSkillKeys())
                                                .executes(
                                                        FabricSkillLevelCommands::executeResetOne)));
    }

    private static RequiredArgumentBuilder<ServerCommandSource, String> wrappedPlayerSkillInt(
            com.mojang.brigadier.arguments.IntegerArgumentType intType, String intArg, com.mojang.brigadier.Command<ServerCommandSource> end) {
        return CommandManager.argument(ARG_PLAYER, StringArgumentType.word())
                .suggests(FabricCommandSuggestionProviders.onlinePlayers())
                .then(
                        CommandManager.argument(ARG_SKILL, StringArgumentType.word())
                                .suggests(FabricCommandSuggestionProviders.enabledSkillKeys())
                                .then(CommandManager.argument(intArg, intType).executes(end)));
    }

    private static int executeView(CommandContext<ServerCommandSource> ctx) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            ServerCommandSource source = ctx.getSource();
                            String name = StringArgumentType.getString(ctx, ARG_PLAYER);
                            String rawSkill = StringArgumentType.getString(ctx, ARG_SKILL);
                            ServerPlayerEntity target =
                                    FabricCommandPlayerTarget.findOnline(source.getServer(), name)
                                            .orElse(null);
                            if (target == null) {
                                source.sendError(
                                        Text.translatable(
                                                "auraskills.granite.command.xp.player_not_found",
                                                name));
                                return 0;
                            }
                            Skill skill = FabricCommandSkillParser.parse(p, rawSkill);
                            if (skill == null) {
                                User u = p.getUserManager().getUser(target.getUuid());
                                unknownSkill(source, p, u);
                                return 0;
                            }
                            User user = p.getUserManager().getUser(target.getUuid());
                            if (user == null) {
                                source.sendError(
                                        Text.literal(
                                                p.getMsg(
                                                        CommandMessage.NO_PROFILE,
                                                        p.getDefaultLanguage())));
                                return 0;
                            }
                            String display = target.getName().getString();
                            Locale lo = user.getLocale();
                            int level = user.getSkillLevel(skill);
                            double xp = user.getSkillXp(skill);
                            source.sendFeedback(
                                    () -> Text.translatable(
                                            "auraskills.granite.command.skilllevel.view_line",
                                            display,
                                            skill.getDisplayName(lo),
                                            level,
                                            NumberUtil.format2(xp)),
                                    false);
                            return 1;
                        })
                .orElseGet(
                        () -> {
                            ctx.getSource()
                                    .sendError(Text.literal("AuraSkills is still starting."));
                            return 0;
                        });
    }

    private static int executeAdd(CommandContext<ServerCommandSource> ctx) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            int delta = IntegerArgumentType.getInteger(ctx, ARG_LEVELS);
                            if (!FabricCommandAmountRules.requirePositiveIntForAdd(delta, ctx.getSource())) {
                                return 0;
                            }
                            return withTargetSkill(
                                    ctx,
                                    p,
                                    (user, skill, targetPlayer) -> {
                                        int start = p.config().getStartLevel();
                                        int next = user.getSkillLevel(skill) + delta;
                                        if (next < start) {
                                            // Should not happen with positive delta and valid profile
                                            sendAtLeast(
                                                    p, ctx.getSource(), user, start);
                                            return 0;
                                        }
                                        FabricCommandSkillLevelApply.setSkillLevel(
                                                p, user, targetPlayer, skill, next);
                                        feedbackSet(
                                                p,
                                                ctx.getSource(),
                                                user.getLocale(),
                                                skill,
                                                next,
                                                targetPlayer.getName().getString());
                                        return 1;
                                    });
                        })
                .orElseGet(
                        () -> {
                            ctx.getSource()
                                    .sendError(Text.literal("AuraSkills is still starting."));
                            return 0;
                        });
    }

    private static int executeSet(CommandContext<ServerCommandSource> ctx) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            int newLevel = IntegerArgumentType.getInteger(ctx, ARG_LEVEL);
                            return withTargetSkill(
                                    ctx,
                                    p,
                                    (user, skill, targetPlayer) -> {
                                        int start = p.config().getStartLevel();
                                        if (newLevel < start) {
                                            sendAtLeast(p, ctx.getSource(), user, start);
                                            return 0;
                                        }
                                        FabricCommandSkillLevelApply.setSkillLevel(
                                                p, user, targetPlayer, skill, newLevel);
                                        feedbackSet(
                                                p,
                                                ctx.getSource(),
                                                user.getLocale(),
                                                skill,
                                                newLevel,
                                                targetPlayer.getName().getString());
                                        return 1;
                                    });
                        })
                .orElseGet(
                        () -> {
                            ctx.getSource()
                                    .sendError(Text.literal("AuraSkills is still starting."));
                            return 0;
                        });
    }

    private static int executeRemove(CommandContext<ServerCommandSource> ctx) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            int toRemove = IntegerArgumentType.getInteger(ctx, ARG_LEVELS);
                            if (!FabricCommandAmountRules.requirePositiveIntForAdd(toRemove, ctx.getSource())) {
                                return 0;
                            }
                            return withTargetSkill(
                                    ctx,
                                    p,
                                    (user, skill, targetPlayer) -> {
                                        int start = p.config().getStartLevel();
                                        int current = user.getSkillLevel(skill);
                                        int newLevel = FabricCommandSkillLevelApply.clampRemoveTarget(
                                                start, current, toRemove);
                                        FabricCommandSkillLevelApply.setSkillLevel(
                                                p, user, targetPlayer, skill, newLevel);
                                        feedbackSet(
                                                p,
                                                ctx.getSource(),
                                                user.getLocale(),
                                                skill,
                                                newLevel,
                                                targetPlayer.getName().getString());
                                        return 1;
                                    });
                        })
                .orElseGet(
                        () -> {
                            ctx.getSource()
                                    .sendError(Text.literal("AuraSkills is still starting."));
                            return 0;
                        });
    }

    private static int executeSetAll(CommandContext<ServerCommandSource> ctx) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            int level = IntegerArgumentType.getInteger(ctx, ARG_LEVEL);
                            return withTargetPlayer(
                                    ctx,
                                    p,
                                    (user, targetPlayer) -> {
                                        int start = p.config().getStartLevel();
                                        if (level < start) {
                                            sendAtLeast(
                                                    p, ctx.getSource(), user, start);
                                            return 0;
                                        }
                                        FabricCommandSkillLevelApply.setAllEnabledSkillsToLevel(
                                                p, user, level);
                                        feedbackSetAll(
                                                p,
                                                ctx.getSource(),
                                                user.getLocale(),
                                                level,
                                                targetPlayer.getName().getString());
                                        return 1;
                                    });
                        })
                .orElseGet(
                        () -> {
                            ctx.getSource()
                                    .sendError(Text.literal("AuraSkills is still starting."));
                            return 0;
                        });
    }

    private static int executeResetAll(CommandContext<ServerCommandSource> ctx) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            String name = StringArgumentType.getString(ctx, ARG_PLAYER);
                            ServerPlayerEntity target =
                                    FabricCommandPlayerTarget.findOnline(
                                                    ctx.getSource().getServer(), name)
                                            .orElse(null);
                            if (target == null) {
                                ctx.getSource()
                                        .sendError(
                                                Text.translatable(
                                                        "auraskills.granite.command.xp.player_not_found",
                                                        name));
                                return 0;
                            }
                            User user = p.getUserManager().getUser(target.getUuid());
                            if (user == null) {
                                ctx.getSource()
                                        .sendError(
                                                Text.literal(
                                                        p.getMsg(
                                                                CommandMessage.NO_PROFILE,
                                                                p.getDefaultLanguage())));
                                return 0;
                            }
                            int last = p.config().getStartLevel();
                            for (Skill s : p.getSkillRegistry().getValues()) {
                                last = user.resetSkill(s);
                            }
                            FabricCommandSkillLevelApply.afterSkillResetApplyModifiers(
                                    p, user);
                            feedbackSetAll(
                                    p,
                                    ctx.getSource(),
                                    user.getLocale(),
                                    last,
                                    target.getName().getString());
                            return 1;
                        })
                .orElseGet(
                        () -> {
                            ctx.getSource()
                                    .sendError(Text.literal("AuraSkills is still starting."));
                            return 0;
                        });
    }

    private static int executeResetOne(CommandContext<ServerCommandSource> ctx) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            String name = StringArgumentType.getString(ctx, ARG_PLAYER);
                            String rawSkill = StringArgumentType.getString(ctx, ARG_SKILL);
                            ServerCommandSource source = ctx.getSource();
                            ServerPlayerEntity target =
                                    FabricCommandPlayerTarget.findOnline(source.getServer(), name)
                                            .orElse(null);
                            if (target == null) {
                                source.sendError(
                                        Text.translatable(
                                                "auraskills.granite.command.xp.player_not_found",
                                                name));
                                return 0;
                            }
                            User user = p.getUserManager().getUser(target.getUuid());
                            if (user == null) {
                                source.sendError(
                                        Text.literal(
                                                p.getMsg(
                                                        CommandMessage.NO_PROFILE,
                                                        p.getDefaultLanguage())));
                                return 0;
                            }
                            Skill skill = FabricCommandSkillParser.parse(p, rawSkill);
                            if (skill == null) {
                                unknownSkill(source, p, user);
                                return 0;
                            }
                            int newLevel = user.resetSkill(skill);
                            FabricCommandSkillLevelApply.afterSkillResetApplyModifiers(
                                    p, user);
                            feedbackSet(
                                    p,
                                    source,
                                    user.getLocale(),
                                    skill,
                                    newLevel,
                                    target.getName().getString());
                            return 1;
                        })
                .orElseGet(
                        () -> {
                            ctx.getSource()
                                    .sendError(Text.literal("AuraSkills is still starting."));
                            return 0;
                        });
    }

    @FunctionalInterface
    private interface PlayerOp {
        int run(User user, ServerPlayerEntity targetPlayer);
    }

    private static int withTargetPlayer(
            CommandContext<ServerCommandSource> ctx, FabricAuraSkills p, PlayerOp op) {
        ServerCommandSource source = ctx.getSource();
        String name = StringArgumentType.getString(ctx, ARG_PLAYER);
        ServerPlayerEntity target =
                FabricCommandPlayerTarget.findOnline(source.getServer(), name).orElse(null);
        if (target == null) {
            source.sendError(
                    Text.translatable("auraskills.granite.command.xp.player_not_found", name));
            return 0;
        }
        User user = p.getUserManager().getUser(target.getUuid());
        if (user == null) {
            source.sendError(
                    Text.literal(
                            p.getMsg(CommandMessage.NO_PROFILE, p.getDefaultLanguage())));
            return 0;
        }
        return op.run(user, target);
    }

    @FunctionalInterface
    private interface TargetOp {
        int run(User user, Skill skill, ServerPlayerEntity targetPlayer);
    }

    private static int withTargetSkill(
            CommandContext<ServerCommandSource> ctx, FabricAuraSkills p, TargetOp op) {
        ServerCommandSource source = ctx.getSource();
        String name = StringArgumentType.getString(ctx, ARG_PLAYER);
        String rawSkill = StringArgumentType.getString(ctx, ARG_SKILL);
        ServerPlayerEntity target =
                FabricCommandPlayerTarget.findOnline(source.getServer(), name).orElse(null);
        if (target == null) {
            source.sendError(
                    Text.translatable("auraskills.granite.command.xp.player_not_found", name));
            return 0;
        }
        Skill skill = FabricCommandSkillParser.parse(p, rawSkill);
        if (skill == null) {
            User u = p.getUserManager().getUser(target.getUuid());
            unknownSkill(source, p, u);
            return 0;
        }
        User user = p.getUserManager().getUser(target.getUuid());
        if (user == null) {
            source.sendError(
                    Text.literal(
                            p.getMsg(CommandMessage.NO_PROFILE, p.getDefaultLanguage())));
            return 0;
        }
        return op.run(user, skill, target);
    }

    private static void feedbackSet(
            FabricAuraSkills p,
            ServerCommandSource source,
            Locale locale,
            Skill skill,
            int newLevel,
            String playerName) {
        FabricCommandMutationFeedback.sendPrefixedMessageKey(
                p,
                source,
                locale,
                CommandMessage.SKILL_SETLEVEL_SET,
                "skill",
                skill.getDisplayName(locale),
                "level",
                String.valueOf(newLevel),
                "player",
                playerName);
    }

    private static void feedbackSetAll(
            FabricAuraSkills p,
            ServerCommandSource source,
            Locale locale,
            int level,
            String playerName) {
        FabricCommandMutationFeedback.sendPrefixedMessageKey(
                p,
                source,
                locale,
                CommandMessage.SKILL_SETALL_SET,
                "level",
                String.valueOf(level),
                "player",
                playerName);
    }

    private static void sendAtLeast(
            FabricAuraSkills p, ServerCommandSource source, User user, int start) {
        FabricCommandMutationFeedback.sendPrefixedMessageKey(
                p,
                source,
                user.getLocale(),
                CommandMessage.SKILL_AT_LEAST,
                "level",
                String.valueOf(start));
    }

    private static void unknownSkill(ServerCommandSource source, FabricAuraSkills p, User localeUser) {
        Locale locale = localeUser != null ? localeUser.getLocale() : p.getDefaultLanguage();
        source.sendError(Text.literal(p.getMsg(CommandMessage.UNKNOWN_SKILL, locale)));
    }
}
