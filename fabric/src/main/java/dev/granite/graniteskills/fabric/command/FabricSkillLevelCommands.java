package dev.granite.graniteskills.fabric.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.api.util.NumberUtil;
import dev.granite.graniteskills.common.message.type.CommandMessage;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import dev.granite.graniteskills.fabric.text.FabricGraniteText;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Locale;

import static net.minecraft.commands.Commands.literal;

/**
 * Bounded **skill level** family (Sprints 42–43), aligned with Bukkit
 * {@link dev.granite.graniteskills.bukkit.commands.SkillCommand} ({@code setlevel},
 * {@code addlevel}, {@code setall}, {@code reset}, plus a Fabric {@code remove}). Registered
 * as {@code /skilllevel …} and {@code /graniteskills skilllevel …}.
 */
public final class FabricSkillLevelCommands {

    private static final String ARG_PLAYER = "player";
    private static final String ARG_SKILL = "skill";
    private static final String ARG_LEVEL = "level";
    private static final String ARG_LEVELS = "levels";

    private FabricSkillLevelCommands() {}

    public static LiteralArgumentBuilder<CommandSourceStack> skillLevelRoot() {
        return literal("skilllevel")
                .then(view())
                .then(add())
                .then(set())
                .then(remove())
                .then(setall())
                .then(reset());
    }

    private static LiteralArgumentBuilder<CommandSourceStack> view() {
        return literal("view")
                .requires(
                        FabricCommandRequirements.permission(
                                FabricCommandPermissions.SKILL_LEVEL_VIEW, true))
                .then(
                        Commands.argument(ARG_PLAYER, StringArgumentType.word())
                                .suggests(FabricCommandSuggestionProviders.onlinePlayers())
                                .then(
                                        Commands.argument(ARG_SKILL, StringArgumentType.word())
                                                .suggests(
                                                        FabricCommandSuggestionProviders
                                                                .enabledSkillKeys())
                                                .executes(FabricSkillLevelCommands::executeView)));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> add() {
        return literal("add")
                .requires(
                        FabricCommandRequirements.permission(
                                FabricCommandPermissions.SKILL_SETLEVEL, false))
                .then(wrappedPlayerSkillInt(
                        IntegerArgumentType.integer(1), ARG_LEVELS, FabricSkillLevelCommands::executeAdd));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> set() {
        return literal("set")
                .requires(
                        FabricCommandRequirements.permission(
                                FabricCommandPermissions.SKILL_SETLEVEL, false))
                .then(wrappedPlayerSkillInt(
                        IntegerArgumentType.integer(0),
                        ARG_LEVEL,
                        FabricSkillLevelCommands::executeSet));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> remove() {
        return literal("remove")
                .requires(
                        FabricCommandRequirements.permission(
                                FabricCommandPermissions.SKILL_SETLEVEL, false))
                .then(wrappedPlayerSkillInt(
                        IntegerArgumentType.integer(1),
                        ARG_LEVELS,
                        FabricSkillLevelCommands::executeRemove));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> setall() {
        return literal("setall")
                .requires(
                        FabricCommandRequirements.permission(
                                FabricCommandPermissions.SKILL_SETLEVEL, false))
                .then(
                        Commands.argument(ARG_PLAYER, StringArgumentType.word())
                                .suggests(FabricCommandSuggestionProviders.onlinePlayers())
                                .then(
                                        Commands.argument(
                                                        ARG_LEVEL, IntegerArgumentType.integer(0))
                                                .executes(FabricSkillLevelCommands::executeSetAll)));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> reset() {
        return literal("reset")
                .requires(
                        FabricCommandRequirements.permission(
                                FabricCommandPermissions.SKILL_RESET, false))
                .then(
                        Commands.argument(ARG_PLAYER, StringArgumentType.word())
                                .suggests(FabricCommandSuggestionProviders.onlinePlayers())
                                .executes(FabricSkillLevelCommands::executeResetAll)
                                .then(
                                        Commands.argument(ARG_SKILL, StringArgumentType.word())
                                                .suggests(
                                                        FabricCommandSuggestionProviders
                                                                .enabledSkillKeys())
                                                .executes(
                                                        FabricSkillLevelCommands::executeResetOne)));
    }

    private static RequiredArgumentBuilder<CommandSourceStack, String> wrappedPlayerSkillInt(
            com.mojang.brigadier.arguments.IntegerArgumentType intType, String intArg, com.mojang.brigadier.Command<CommandSourceStack> end) {
        return Commands.argument(ARG_PLAYER, StringArgumentType.word())
                .suggests(FabricCommandSuggestionProviders.onlinePlayers())
                .then(
                        Commands.argument(ARG_SKILL, StringArgumentType.word())
                                .suggests(FabricCommandSuggestionProviders.enabledSkillKeys())
                                .then(Commands.argument(intArg, intType).executes(end)));
    }

    private static int executeView(CommandContext<CommandSourceStack> ctx) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            CommandSourceStack source = ctx.getSource();
                            String name = StringArgumentType.getString(ctx, ARG_PLAYER);
                            String rawSkill = StringArgumentType.getString(ctx, ARG_SKILL);
                            ServerPlayer target =
                                    FabricCommandPlayerTarget.findOnline(source.getServer(), name)
                                            .orElse(null);
                            if (target == null) {
                                source.sendFailure(
                                        FabricGraniteText.key(
                                                "graniteskills.granite.command.xp.player_not_found",
                                                name));
                                return 0;
                            }
                            Skill skill = FabricCommandSkillParser.parse(p, rawSkill);
                            if (skill == null) {
                                User u = p.getUserManager().getUser(target.getUUID());
                                unknownSkill(source, p, u);
                                return 0;
                            }
                            User user = p.getUserManager().getUser(target.getUUID());
                            if (user == null) {
                                source.sendFailure(
                                        Component.literal(
                                                p.getMsg(
                                                        CommandMessage.NO_PROFILE,
                                                        p.getDefaultLanguage())));
                                return 0;
                            }
                            String display = target.getName().getString();
                            Locale lo = user.getLocale();
                            int level = user.getSkillLevel(skill);
                            double xp = user.getSkillXp(skill);
                            source.sendSuccess(
                                    () ->
                                            FabricGraniteText.key(
                                                    "graniteskills.granite.command.skilllevel"
                                                            + ".view_line",
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
                                    .sendFailure(Component.literal("GraniteSkills is still starting."));
                            return 0;
                        });
    }

    private static int executeAdd(CommandContext<CommandSourceStack> ctx) {
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
                                    .sendFailure(Component.literal("GraniteSkills is still starting."));
                            return 0;
                        });
    }

    private static int executeSet(CommandContext<CommandSourceStack> ctx) {
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
                                    .sendFailure(Component.literal("GraniteSkills is still starting."));
                            return 0;
                        });
    }

    private static int executeRemove(CommandContext<CommandSourceStack> ctx) {
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
                                    .sendFailure(Component.literal("GraniteSkills is still starting."));
                            return 0;
                        });
    }

    private static int executeSetAll(CommandContext<CommandSourceStack> ctx) {
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
                                    .sendFailure(Component.literal("GraniteSkills is still starting."));
                            return 0;
                        });
    }

    private static int executeResetAll(CommandContext<CommandSourceStack> ctx) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            String name = StringArgumentType.getString(ctx, ARG_PLAYER);
                            ServerPlayer target =
                                    FabricCommandPlayerTarget.findOnline(
                                                    ctx.getSource().getServer(), name)
                                            .orElse(null);
                            if (target == null) {
                                ctx.getSource()
                                        .sendFailure(
                                                FabricGraniteText.key(
                                                        "graniteskills.granite.command.xp.player_not_found",
                                                        name));
                                return 0;
                            }
                            User user = p.getUserManager().getUser(target.getUUID());
                            if (user == null) {
                                ctx.getSource()
                                        .sendFailure(
                                                Component.literal(
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
                                    .sendFailure(Component.literal("GraniteSkills is still starting."));
                            return 0;
                        });
    }

    private static int executeResetOne(CommandContext<CommandSourceStack> ctx) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            String name = StringArgumentType.getString(ctx, ARG_PLAYER);
                            String rawSkill = StringArgumentType.getString(ctx, ARG_SKILL);
                            CommandSourceStack source = ctx.getSource();
                            ServerPlayer target =
                                    FabricCommandPlayerTarget.findOnline(source.getServer(), name)
                                            .orElse(null);
                            if (target == null) {
                                source.sendFailure(
                                        FabricGraniteText.key(
                                                "graniteskills.granite.command.xp.player_not_found",
                                                name));
                                return 0;
                            }
                            User user = p.getUserManager().getUser(target.getUUID());
                            if (user == null) {
                                source.sendFailure(
                                        Component.literal(
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
                                    .sendFailure(Component.literal("GraniteSkills is still starting."));
                            return 0;
                        });
    }

    @FunctionalInterface
    private interface PlayerOp {
        int run(User user, ServerPlayer targetPlayer);
    }

    private static int withTargetPlayer(
            CommandContext<CommandSourceStack> ctx, FabricGraniteSkills p, PlayerOp op) {
        CommandSourceStack source = ctx.getSource();
        String name = StringArgumentType.getString(ctx, ARG_PLAYER);
        ServerPlayer target =
                FabricCommandPlayerTarget.findOnline(source.getServer(), name).orElse(null);
        if (target == null) {
            source.sendFailure(
                    FabricGraniteText.key(
                            "graniteskills.granite.command.xp.player_not_found", name));
            return 0;
        }
        User user = p.getUserManager().getUser(target.getUUID());
        if (user == null) {
            source.sendFailure(
                    Component.literal(
                            p.getMsg(CommandMessage.NO_PROFILE, p.getDefaultLanguage())));
            return 0;
        }
        return op.run(user, target);
    }

    @FunctionalInterface
    private interface TargetOp {
        int run(User user, Skill skill, ServerPlayer targetPlayer);
    }

    private static int withTargetSkill(
            CommandContext<CommandSourceStack> ctx, FabricGraniteSkills p, TargetOp op) {
        CommandSourceStack source = ctx.getSource();
        String name = StringArgumentType.getString(ctx, ARG_PLAYER);
        String rawSkill = StringArgumentType.getString(ctx, ARG_SKILL);
        ServerPlayer target =
                FabricCommandPlayerTarget.findOnline(source.getServer(), name).orElse(null);
        if (target == null) {
            source.sendFailure(
                    FabricGraniteText.key(
                            "graniteskills.granite.command.xp.player_not_found", name));
            return 0;
        }
        Skill skill = FabricCommandSkillParser.parse(p, rawSkill);
        if (skill == null) {
            User u = p.getUserManager().getUser(target.getUUID());
            unknownSkill(source, p, u);
            return 0;
        }
        User user = p.getUserManager().getUser(target.getUUID());
        if (user == null) {
            source.sendFailure(
                    Component.literal(
                            p.getMsg(CommandMessage.NO_PROFILE, p.getDefaultLanguage())));
            return 0;
        }
        return op.run(user, skill, target);
    }

    private static void feedbackSet(
            FabricGraniteSkills p,
            CommandSourceStack source,
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
            FabricGraniteSkills p,
            CommandSourceStack source,
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
            FabricGraniteSkills p, CommandSourceStack source, User user, int start) {
        FabricCommandMutationFeedback.sendPrefixedMessageKey(
                p,
                source,
                user.getLocale(),
                CommandMessage.SKILL_AT_LEAST,
                "level",
                String.valueOf(start));
    }

    private static void unknownSkill(CommandSourceStack source, FabricGraniteSkills p, User localeUser) {
        Locale locale = localeUser != null ? localeUser.getLocale() : p.getDefaultLanguage();
        source.sendFailure(Component.literal(p.getMsg(CommandMessage.UNKNOWN_SKILL, locale)));
    }
}
