package dev.aurelium.auraskills.fabric.command;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.util.NumberUtil;
import dev.aurelium.auraskills.common.message.type.CommandMessage;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.common.util.text.TextUtil;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Locale;

import static net.minecraft.server.command.CommandManager.literal;

/**
 * {@code xp} family: **view** (read), **add** / **set** / **remove** (mutate) — Bukkit
 * {@code XpCommand} + read-only view (**Sprint 40**). Registered as both {@code /xp …} and
 * {@code /auraskills xp …}. Use {@code <player> <skill>} for **view** (include your own name to
 * see yourself).
 */
public final class FabricXpCommands {

    private static final String ARG_PLAYER = "player";
    private static final String ARG_SKILL = "skill";
    private static final String ARG_AMOUNT = "amount";

    private FabricXpCommands() {}

    public static LiteralArgumentBuilder<ServerCommandSource> xpRoot() {
        return literal("xp")
                .then(xpView())
                .then(xpAdd())
                .then(xpSet())
                .then(xpRemove());
    }

    private static LiteralArgumentBuilder<ServerCommandSource> xpView() {
        return literal("view")
                .requires(FabricCommandRequirements.permission(FabricCommandPermissions.XP_VIEW, true))
                .then(CommandManager.argument(ARG_PLAYER, StringArgumentType.word())
                        .suggests(FabricCommandSuggestionProviders.onlinePlayers())
                        .then(CommandManager.argument(ARG_SKILL, StringArgumentType.word())
                                .suggests(FabricCommandSuggestionProviders.enabledSkillKeys())
                                .executes(FabricXpCommands::executeView)));
    }

    private static LiteralArgumentBuilder<ServerCommandSource> xpAdd() {
        return literal("add")
                .requires(FabricCommandRequirements.permission(FabricCommandPermissions.XP_ADD, false))
                .then(xpTargetSkillAmount(FabricXpCommands::executeAdd));
    }

    private static LiteralArgumentBuilder<ServerCommandSource> xpSet() {
        return literal("set")
                .requires(FabricCommandRequirements.permission(FabricCommandPermissions.XP_SET, false))
                .then(xpTargetSkillAmount(FabricXpCommands::executeSet));
    }

    private static LiteralArgumentBuilder<ServerCommandSource> xpRemove() {
        return literal("remove")
                .requires(FabricCommandRequirements.permission(FabricCommandPermissions.XP_REMOVE, false))
                .then(xpTargetSkillAmount(FabricXpCommands::executeRemove));
    }

    private static RequiredArgumentBuilder<ServerCommandSource, String> xpTargetSkillAmount(
            com.mojang.brigadier.Command<ServerCommandSource> end) {
        return CommandManager.argument(ARG_PLAYER, StringArgumentType.word())
                .suggests(FabricCommandSuggestionProviders.onlinePlayers())
                .then(CommandManager.argument(ARG_SKILL, StringArgumentType.word())
                        .suggests(FabricCommandSuggestionProviders.enabledSkillKeys())
                        .then(CommandManager.argument(ARG_AMOUNT, DoubleArgumentType.doubleArg(0.0))
                                .executes(end)));
    }

    private static int executeView(CommandContext<ServerCommandSource> ctx) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(p -> {
                    ServerCommandSource source = ctx.getSource();
                    String playerName = StringArgumentType.getString(ctx, ARG_PLAYER);
                    String skillRaw = StringArgumentType.getString(ctx, ARG_SKILL);
                    ServerPlayerEntity target =
                            FabricCommandPlayerTarget.findOnline(source.getServer(), playerName)
                                    .orElse(null);
                    if (target == null) {
                        source.sendError(Text.translatable(
                                "auraskills.granite.command.xp.player_not_found", playerName));
                        return 0;
                    }
                    Skill skill = FabricCommandSkillParser.parse(p, skillRaw);
                    if (skill == null) {
                        User u = p.getUserManager().getUser(target.getUuid());
                        unknownSkill(source, p, u);
                        return 0;
                    }
                    User user = p.getUserManager().getUser(target.getUuid());
                    if (user == null) {
                        source.sendError(Text.literal(
                                p.getMsg(CommandMessage.NO_PROFILE, p.getDefaultLanguage())));
                        return 0;
                    }
                    Locale locale = senderLocale(p, source);
                    double xp = user.getSkillXp(skill);
                    int level = user.getSkillLevel(skill);
                    String display = target.getName().getString();
                    source.sendFeedback(
                            () -> Text.translatable(
                                    "auraskills.granite.command.xp.view_line",
                                    display,
                                    skill.getDisplayName(locale),
                                    level,
                                    NumberUtil.format2(xp)),
                            false);
                    return 1;
                })
                .orElseGet(() -> {
                    ctx.getSource().sendError(Text.literal("AuraSkills is still starting."));
                    return 0;
                });
    }

    private static int executeAdd(CommandContext<ServerCommandSource> ctx) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(p -> {
                    double amount = DoubleArgumentType.getDouble(ctx, ARG_AMOUNT);
                    if (amount <= 0.0) {
                        ctx.getSource()
                                .sendError(Text.translatable("auraskills.granite.command.xp.amount_positive"));
                        return 0;
                    }
                    return mutate(
                            ctx,
                            p,
                            (user, skill, targetPlayer) -> {
                                p.getLevelManager().addXp(user, skill, null, amount);
                                return feedback(
                                        p,
                                        ctx.getSource(),
                                        user,
                                        CommandMessage.XP_ADD_ADDED,
                                        amount,
                                        skill,
                                        targetPlayer);
                            });
                })
                .orElseGet(() -> {
                    ctx.getSource().sendError(Text.literal("AuraSkills is still starting."));
                    return 0;
                });
    }

    private static int executeSet(CommandContext<ServerCommandSource> ctx) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(p -> mutate(
                        ctx,
                        p,
                        (user, skill, targetPlayer) -> {
                            double amount = DoubleArgumentType.getDouble(ctx, ARG_AMOUNT);
                            p.getLevelManager().setXp(user, skill, amount);
                            return feedback(
                                    p,
                                    ctx.getSource(),
                                    user,
                                    CommandMessage.XP_SET_SET,
                                    amount,
                                    skill,
                                    targetPlayer);
                        }))
                .orElseGet(() -> {
                    ctx.getSource().sendError(Text.literal("AuraSkills is still starting."));
                    return 0;
                });
    }

    private static int executeRemove(CommandContext<ServerCommandSource> ctx) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(p -> {
                    double amount = DoubleArgumentType.getDouble(ctx, ARG_AMOUNT);
                    if (amount < 0.0) {
                        ctx.getSource()
                                .sendError(Text.translatable("auraskills.granite.command.xp.amount_non_negative"));
                        return 0;
                    }
                    return mutate(
                            ctx,
                            p,
                            (user, skill, targetPlayer) -> {
                                double cur = user.getSkillXp(skill);
                                double remove = Math.min(amount, cur);
                                p.getLevelManager().setXp(user, skill, cur - remove);
                                ServerCommandSource source = ctx.getSource();
                                Locale loc = user.getLocale();
                                String msg = p.getPrefix(loc)
                                        + TextUtil.replace(
                                                p.getMsg(CommandMessage.XP_REMOVE_REMOVED, loc),
                                                "{amount}",
                                                String.valueOf(remove),
                                                "{skill}",
                                                skill.getDisplayName(loc),
                                                "{player}",
                                                targetPlayer.getName().getString());
                                source.sendFeedback(() -> Text.literal(msg), true);
                                return 1;
                            });
                })
                .orElseGet(() -> {
                    ctx.getSource().sendError(Text.literal("AuraSkills is still starting."));
                    return 0;
                });
    }

    @FunctionalInterface
    private interface XpMutator {
        int apply(User user, Skill skill, ServerPlayerEntity targetPlayer) throws RuntimeException;
    }

    private static int mutate(CommandContext<ServerCommandSource> ctx, FabricAuraSkills p, XpMutator mutator) {
        ServerCommandSource source = ctx.getSource();
        String playerName = StringArgumentType.getString(ctx, ARG_PLAYER);
        String skillRaw = StringArgumentType.getString(ctx, ARG_SKILL);
        ServerPlayerEntity target =
                FabricCommandPlayerTarget.findOnline(source.getServer(), playerName).orElse(null);
        if (target == null) {
            source.sendError(Text.translatable(
                    "auraskills.granite.command.xp.player_not_found", playerName));
            return 0;
        }
        Skill skill = FabricCommandSkillParser.parse(p, skillRaw);
        if (skill == null) {
            User u = p.getUserManager().getUser(target.getUuid());
            unknownSkill(source, p, u);
            return 0;
        }
        User user = p.getUserManager().getUser(target.getUuid());
        if (user == null) {
            source.sendError(Text.literal(
                    p.getMsg(CommandMessage.NO_PROFILE, p.getDefaultLanguage())));
            return 0;
        }
        return mutator.apply(user, skill, target);
    }

    private static int feedback(
            FabricAuraSkills plugin,
            ServerCommandSource source,
            User user,
            CommandMessage message,
            double amount,
            Skill skill,
            ServerPlayerEntity targetPlayer) {
        Locale locale = user.getLocale();
        String msg = plugin.getPrefix(locale)
                + TextUtil.replace(
                        plugin.getMsg(message, locale),
                        "{amount}",
                        String.valueOf(amount),
                        "{skill}",
                        skill.getDisplayName(locale),
                        "{player}",
                        targetPlayer.getName().getString());
        source.sendFeedback(() -> Text.literal(msg), true);
        return 1;
    }

    private static void unknownSkill(ServerCommandSource source, FabricAuraSkills p, User localeUser) {
        Locale locale = localeUser != null ? localeUser.getLocale() : p.getDefaultLanguage();
        source.sendError(Text.literal(p.getMsg(CommandMessage.UNKNOWN_SKILL, locale)));
    }

    private static Locale senderLocale(FabricAuraSkills p, ServerCommandSource source) {
        if (source.getEntity() instanceof ServerPlayerEntity sp) {
            User u = p.getUserManager().getUser(sp.getUuid());
            if (u != null) {
                return u.getLocale();
            }
        }
        return p.getDefaultLanguage();
    }
}
