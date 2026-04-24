package dev.aurelium.auraskills.fabric.command;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.aurelium.auraskills.api.trait.Traits;
import dev.aurelium.auraskills.api.util.NumberUtil;
import dev.aurelium.auraskills.common.message.MessageBuilder;
import dev.aurelium.auraskills.common.message.type.CommandMessage;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Locale;

import static net.minecraft.server.command.CommandManager.literal;

/**
 * Full bounded **mana** family for Fabric (Sprint 41). Shape follows
 * {@link dev.aurelium.auraskills.common.commands.ManaCommand}; admin subcommands use
 * {@link FabricCommandMutationFeedback} / {@link FabricCommandAmountRules}. Registered as
 * {@code /mana …} and {@code /auraskills mana …}.
 */
public final class FabricManaCommands {

    private static final String ARG_PLAYER = "player";
    private static final String ARG_AMOUNT = "amount";

    private FabricManaCommands() {}

    public static LiteralArgumentBuilder<ServerCommandSource> manaCommandRoot() {
        return literal("mana")
                .executes(FabricManaCommands::executeSelf)
                .then(literal("view")
                        .requires(FabricCommandRequirements.permission(FabricCommandPermissions.MANA_OTHER, false))
                        .then(CommandManager.argument(ARG_PLAYER, StringArgumentType.word())
                                .suggests(FabricCommandSuggestionProviders.onlinePlayers())
                                .executes(FabricManaCommands::executeView)))
                .then(literal("add")
                        .requires(FabricCommandRequirements.permission(FabricCommandPermissions.MANA_ADD, false))
                        .then(playerAmount(FabricManaCommands::executeAdd)))
                .then(literal("set")
                        .requires(FabricCommandRequirements.permission(FabricCommandPermissions.MANA_SET, false))
                        .then(playerAmount(FabricManaCommands::executeSet)))
                .then(literal("remove")
                        .requires(FabricCommandRequirements.permission(FabricCommandPermissions.MANA_REMOVE, false))
                        .then(playerAmount(FabricManaCommands::executeRemove)));
    }

    private static RequiredArgumentBuilder<ServerCommandSource, String> playerAmount(
            com.mojang.brigadier.Command<ServerCommandSource> end) {
        return CommandManager.argument(ARG_PLAYER, StringArgumentType.word())
                .suggests(FabricCommandSuggestionProviders.onlinePlayers())
                .then(CommandManager.argument(ARG_AMOUNT, DoubleArgumentType.doubleArg(0.0)).executes(end));
    }

    private static int executeSelf(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();
        if (!Permissions.check(source, FabricCommandPermissions.MANA, true)) {
            source.sendError(Text.translatable("auraskills.granite.command.permission_denied"));
            return 0;
        }
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(p -> {
                    if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
                        source.sendError(Text.translatable("auraskills.granite.command.mana.console_specify"));
                        return 0;
                    }
                    User user = p.getUserManager().getUser(player.getUuid());
                    if (user == null) {
                        source.sendError(Text.literal(
                                p.getMsg(CommandMessage.NO_PROFILE, p.getDefaultLanguage())));
                        return 0;
                    }
                    MessageBuilder.create(p)
                            .locale(user.getLocale())
                            .prefix()
                            .message(
                                    CommandMessage.MANA_DISPLAY,
                                    "current",
                                    NumberUtil.format1(user.getMana()),
                                    "max",
                                    NumberUtil.format1(user.getMaxMana()))
                            .send(user);
                    return 1;
                })
                .orElseGet(() -> {
                    source.sendError(Text.literal("AuraSkills is still starting."));
                    return 0;
                });
    }

    private static int executeView(CommandContext<ServerCommandSource> ctx) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(p -> {
                    ServerCommandSource source = ctx.getSource();
                    String name = StringArgumentType.getString(ctx, ARG_PLAYER);
                    ServerPlayerEntity target =
                            FabricCommandPlayerTarget.findOnline(source.getServer(), name).orElse(null);
                    if (target == null) {
                        source.sendError(Text.translatable(
                                "auraskills.granite.command.xp.player_not_found", name));
                        return 0;
                    }
                    User user = p.getUserManager().getUser(target.getUuid());
                    if (user == null) {
                        source.sendError(Text.literal(
                                p.getMsg(CommandMessage.NO_PROFILE, p.getDefaultLanguage())));
                        return 0;
                    }
                    MessageBuilder b = MessageBuilder.create(p)
                            .locale(user.getLocale())
                            .prefix()
                            .message(
                                    CommandMessage.MANA_DISPLAY_OTHER,
                                    "player",
                                    user.getUsername(),
                                    "current",
                                    NumberUtil.format1(user.getMana()),
                                    "max",
                                    NumberUtil.format1(user.getMaxMana()));
                    FabricCommandMutationFeedback.sendToSource(p, source, b, false);
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
                    if (!FabricCommandAmountRules.requirePositiveForAdd(amount, ctx.getSource())) {
                        return 0;
                    }
                    return mutateTarget(
                            ctx,
                            p,
                            (user, targetPlayer) -> {
                                double manaToAdd = amount;
                                if (user.getMana() + manaToAdd > user.getMaxMana()) {
                                    boolean allowOverflow =
                                            Traits.MAX_MANA.isEnabled()
                                                    && Traits.MAX_MANA.optionBoolean("allow_overflow");
                                    if (!allowOverflow) {
                                        manaToAdd = user.getMaxMana() - user.getMana();
                                    }
                                }
                                if (manaToAdd > 0) {
                                    user.setMana(user.getMana() + manaToAdd);
                                }
                                Locale loc = user.getLocale();
                                FabricCommandMutationFeedback.sendPrefixedMessageKey(
                                        p,
                                        ctx.getSource(),
                                        loc,
                                        CommandMessage.MANA_ADD_ADDED,
                                        "amount",
                                        NumberUtil.format2(manaToAdd),
                                        "player",
                                        user.getUsername());
                                return 1;
                            });
                })
                .orElseGet(() -> {
                    ctx.getSource().sendError(Text.literal("AuraSkills is still starting."));
                    return 0;
                });
    }

    private static int executeSet(CommandContext<ServerCommandSource> ctx) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(p -> {
                    double amount = DoubleArgumentType.getDouble(ctx, ARG_AMOUNT);
                    if (!FabricCommandAmountRules.requireNonNegative(amount, ctx.getSource())) {
                        return 0;
                    }
                    return mutateTarget(
                            ctx,
                            p,
                            (user, targetPlayer) -> {
                                double manaToSet = amount;
                                if (manaToSet > user.getMaxMana()) {
                                    if (!Traits.MAX_MANA.optionBoolean("allow_overflow", false)) {
                                        manaToSet = user.getMaxMana();
                                    }
                                }
                                user.setMana(manaToSet);
                                Locale loc = user.getLocale();
                                FabricCommandMutationFeedback.sendPrefixedMessageKey(
                                        p,
                                        ctx.getSource(),
                                        loc,
                                        CommandMessage.MANA_SET_SET,
                                        "amount",
                                        NumberUtil.format2(manaToSet),
                                        "player",
                                        user.getUsername());
                                return 1;
                            });
                })
                .orElseGet(() -> {
                    ctx.getSource().sendError(Text.literal("AuraSkills is still starting."));
                    return 0;
                });
    }

    private static int executeRemove(CommandContext<ServerCommandSource> ctx) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(p -> {
                    double amount = DoubleArgumentType.getDouble(ctx, ARG_AMOUNT);
                    if (!FabricCommandAmountRules.requireNonNegative(amount, ctx.getSource())) {
                        return 0;
                    }
                    return mutateTarget(
                            ctx,
                            p,
                            (user, targetPlayer) -> {
                                double toRemove = amount;
                                if (user.getMana() - toRemove < 0) {
                                    toRemove = user.getMana();
                                }
                                user.setMana(user.getMana() - toRemove);
                                Locale loc = user.getLocale();
                                FabricCommandMutationFeedback.sendPrefixedMessageKey(
                                        p,
                                        ctx.getSource(),
                                        loc,
                                        CommandMessage.MANA_REMOVE_REMOVED,
                                        "amount",
                                        NumberUtil.format2(toRemove),
                                        "player",
                                        user.getUsername());
                                return 1;
                            });
                })
                .orElseGet(() -> {
                    ctx.getSource().sendError(Text.literal("AuraSkills is still starting."));
                    return 0;
                });
    }

    @FunctionalInterface
    private interface TargetMutator {
        int apply(User user, ServerPlayerEntity targetPlayer);
    }

    private static int mutateTarget(CommandContext<ServerCommandSource> ctx, FabricAuraSkills p, TargetMutator m) {
        ServerCommandSource source = ctx.getSource();
        String name = StringArgumentType.getString(ctx, ARG_PLAYER);
        ServerPlayerEntity target =
                FabricCommandPlayerTarget.findOnline(source.getServer(), name).orElse(null);
        if (target == null) {
            source.sendError(Text.translatable("auraskills.granite.command.xp.player_not_found", name));
            return 0;
        }
        User user = p.getUserManager().getUser(target.getUuid());
        if (user == null) {
            source.sendError(Text.literal(
                    p.getMsg(CommandMessage.NO_PROFILE, p.getDefaultLanguage())));
            return 0;
        }
        return m.apply(user, target);
    }
}
