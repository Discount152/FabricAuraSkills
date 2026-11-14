package dev.granite.graniteskills.fabric.command;

import com.mojang.brigadier.arguments.DoubleArgumentType;
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
 * {@code xp} family: **view** (read), **add** / **set** / **remove** (mutate) — Bukkit
 * {@code XpCommand} + read-only view (**Sprint 40**). Registered as both {@code /xp …} and
 * {@code /graniteskills xp …}. Use {@code <player> <skill>} for **view** (include your own name to
 * see yourself).
 */
public final class FabricXpCommands {

    private static final String ARG_PLAYER = "player";
    private static final String ARG_SKILL = "skill";
    private static final String ARG_AMOUNT = "amount";

    private FabricXpCommands() {}

    public static LiteralArgumentBuilder<CommandSourceStack> xpRoot() {
        return literal("xp")
                .then(xpView())
                .then(xpAdd())
                .then(xpSet())
                .then(xpRemove());
    }

    private static LiteralArgumentBuilder<CommandSourceStack> xpView() {
        return literal("view")
                .requires(FabricCommandRequirements.permission(FabricCommandPermissions.XP_VIEW, true))
                .then(Commands.argument(ARG_PLAYER, StringArgumentType.word())
                        .suggests(FabricCommandSuggestionProviders.onlinePlayers())
                        .then(Commands.argument(ARG_SKILL, StringArgumentType.word())
                                .suggests(FabricCommandSuggestionProviders.enabledSkillKeys())
                                .executes(FabricXpCommands::executeView)));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> xpAdd() {
        return literal("add")
                .requires(FabricCommandRequirements.permission(FabricCommandPermissions.XP_ADD, false))
                .then(xpTargetSkillAmount(FabricXpCommands::executeAdd));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> xpSet() {
        return literal("set")
                .requires(FabricCommandRequirements.permission(FabricCommandPermissions.XP_SET, false))
                .then(xpTargetSkillAmount(FabricXpCommands::executeSet));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> xpRemove() {
        return literal("remove")
                .requires(FabricCommandRequirements.permission(FabricCommandPermissions.XP_REMOVE, false))
                .then(xpTargetSkillAmount(FabricXpCommands::executeRemove));
    }

    private static RequiredArgumentBuilder<CommandSourceStack, String> xpTargetSkillAmount(
            com.mojang.brigadier.Command<CommandSourceStack> end) {
        return Commands.argument(ARG_PLAYER, StringArgumentType.word())
                .suggests(FabricCommandSuggestionProviders.onlinePlayers())
                .then(Commands.argument(ARG_SKILL, StringArgumentType.word())
                        .suggests(FabricCommandSuggestionProviders.enabledSkillKeys())
                        .then(Commands.argument(ARG_AMOUNT, DoubleArgumentType.doubleArg(0.0))
                                .executes(end)));
    }

    private static int executeView(CommandContext<CommandSourceStack> ctx) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(p -> {
                    CommandSourceStack source = ctx.getSource();
                    String playerName = StringArgumentType.getString(ctx, ARG_PLAYER);
                    String skillRaw = StringArgumentType.getString(ctx, ARG_SKILL);
                    ServerPlayer target =
                            FabricCommandPlayerTarget.findOnline(source.getServer(), playerName)
                                    .orElse(null);
                    if (target == null) {
                        source.sendFailure(
                                FabricGraniteText.key(
                                        "graniteskills.granite.command.xp.player_not_found",
                                        playerName));
                        return 0;
                    }
                    Skill skill = FabricCommandSkillParser.parse(p, skillRaw);
                    if (skill == null) {
                        User u = p.getUserManager().getUser(target.getUUID());
                        unknownSkill(source, p, u);
                        return 0;
                    }
                    User user = p.getUserManager().getUser(target.getUUID());
                    if (user == null) {
                        source.sendFailure(Component.literal(
                                p.getMsg(CommandMessage.NO_PROFILE, p.getDefaultLanguage())));
                        return 0;
                    }
                    Locale locale = senderLocale(p, source);
                    double xp = user.getSkillXp(skill);
                    int level = user.getSkillLevel(skill);
                    String display = target.getName().getString();
                    source.sendSuccess(
                            () ->
                                    FabricGraniteText.key(
                                            "graniteskills.granite.command.xp.view_line",
                                            display,
                                            skill.getDisplayName(locale),
                                            level,
                                            NumberUtil.format2(xp)),
                            false);
                    return 1;
                })
                .orElseGet(() -> {
                    ctx.getSource().sendFailure(Component.literal("GraniteSkills is still starting."));
                    return 0;
                });
    }

    private static int executeAdd(CommandContext<CommandSourceStack> ctx) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(p -> {
                    double amount = DoubleArgumentType.getDouble(ctx, ARG_AMOUNT);
                    if (!FabricCommandAmountRules.requirePositiveForAdd(amount, ctx.getSource())) {
                        return 0;
                    }
                    return mutate(
                            ctx,
                            p,
                            (user, skill, targetPlayer) -> {
                                p.getLevelManager().addXp(user, skill, null, amount);
                                FabricCommandMutationFeedback.sendXpStyleTriple(
                                        p,
                                        ctx.getSource(),
                                        user.getLocale(),
                                        CommandMessage.XP_ADD_ADDED,
                                        String.valueOf(amount),
                                        skill.getDisplayName(user.getLocale()),
                                        targetPlayer.getName().getString());
                                return 1;
                            });
                })
                .orElseGet(() -> {
                    ctx.getSource().sendFailure(Component.literal("GraniteSkills is still starting."));
                    return 0;
                });
    }

    private static int executeSet(CommandContext<CommandSourceStack> ctx) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(p -> mutate(
                        ctx,
                        p,
                        (user, skill, targetPlayer) -> {
                            double amount = DoubleArgumentType.getDouble(ctx, ARG_AMOUNT);
                            p.getLevelManager().setXp(user, skill, amount);
                            FabricCommandMutationFeedback.sendXpStyleTriple(
                                    p,
                                    ctx.getSource(),
                                    user.getLocale(),
                                    CommandMessage.XP_SET_SET,
                                    String.valueOf(amount),
                                    skill.getDisplayName(user.getLocale()),
                                    targetPlayer.getName().getString());
                            return 1;
                        }))
                .orElseGet(() -> {
                    ctx.getSource().sendFailure(Component.literal("GraniteSkills is still starting."));
                    return 0;
                });
    }

    private static int executeRemove(CommandContext<CommandSourceStack> ctx) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(p -> {
                    double amount = DoubleArgumentType.getDouble(ctx, ARG_AMOUNT);
                    if (!FabricCommandAmountRules.requireNonNegative(amount, ctx.getSource())) {
                        return 0;
                    }
                    return mutate(
                            ctx,
                            p,
                            (user, skill, targetPlayer) -> {
                                double cur = user.getSkillXp(skill);
                                double remove = Math.min(amount, cur);
                                p.getLevelManager().setXp(user, skill, cur - remove);
                                FabricCommandMutationFeedback.sendXpStyleTriple(
                                        p,
                                        ctx.getSource(),
                                        user.getLocale(),
                                        CommandMessage.XP_REMOVE_REMOVED,
                                        String.valueOf(remove),
                                        skill.getDisplayName(user.getLocale()),
                                        targetPlayer.getName().getString());
                                return 1;
                            });
                })
                .orElseGet(() -> {
                    ctx.getSource().sendFailure(Component.literal("GraniteSkills is still starting."));
                    return 0;
                });
    }

    @FunctionalInterface
    private interface XpMutator {
        int apply(User user, Skill skill, ServerPlayer targetPlayer) throws RuntimeException;
    }

    private static int mutate(CommandContext<CommandSourceStack> ctx, FabricGraniteSkills p, XpMutator mutator) {
        CommandSourceStack source = ctx.getSource();
        String playerName = StringArgumentType.getString(ctx, ARG_PLAYER);
        String skillRaw = StringArgumentType.getString(ctx, ARG_SKILL);
        ServerPlayer target =
                FabricCommandPlayerTarget.findOnline(source.getServer(), playerName).orElse(null);
        if (target == null) {
            source.sendFailure(
                    FabricGraniteText.key(
                            "graniteskills.granite.command.xp.player_not_found", playerName));
            return 0;
        }
        Skill skill = FabricCommandSkillParser.parse(p, skillRaw);
        if (skill == null) {
            User u = p.getUserManager().getUser(target.getUUID());
            unknownSkill(source, p, u);
            return 0;
        }
        User user = p.getUserManager().getUser(target.getUUID());
        if (user == null) {
            source.sendFailure(Component.literal(
                    p.getMsg(CommandMessage.NO_PROFILE, p.getDefaultLanguage())));
            return 0;
        }
        return mutator.apply(user, skill, target);
    }

    private static void unknownSkill(CommandSourceStack source, FabricGraniteSkills p, User localeUser) {
        Locale locale = localeUser != null ? localeUser.getLocale() : p.getDefaultLanguage();
        source.sendFailure(Component.literal(p.getMsg(CommandMessage.UNKNOWN_SKILL, locale)));
    }

    private static Locale senderLocale(FabricGraniteSkills p, CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer sp) {
            User u = p.getUserManager().getUser(sp.getUUID());
            if (u != null) {
                return u.getLocale();
            }
        }
        return p.getDefaultLanguage();
    }
}
