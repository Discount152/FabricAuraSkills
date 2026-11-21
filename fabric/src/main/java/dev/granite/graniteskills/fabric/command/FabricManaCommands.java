package dev.granite.graniteskills.fabric.command;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.granite.graniteskills.api.trait.Traits;
import dev.granite.graniteskills.api.util.NumberUtil;
import dev.granite.graniteskills.common.message.MessageBuilder;
import dev.granite.graniteskills.common.message.type.CommandMessage;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import dev.granite.graniteskills.fabric.text.FabricGraniteText;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Locale;

import static net.minecraft.commands.Commands.literal;

/**
 * Full bounded **mana** family for Fabric (Sprint 41). Shape follows
 * {@link dev.granite.graniteskills.common.commands.ManaCommand}; admin subcommands use
 * {@link FabricCommandMutationFeedback} / {@link FabricCommandAmountRules}. Registered as
 * {@code /mana …} and {@code /graniteskills mana …}.
 * <p>
 * <strong>View (Sprint 83):</strong> {@code /mana view} with no second argument reuses
 * {@link #executeSelf} (same as plain {@code /mana}) for players; console must use
 * {@code /mana view &lt;player&gt;} for another player, which still requires
 * {@link FabricCommandPermissions#MANA_OTHER}. Without {@code MANA_OTHER}, the {@code
 * <player>} branch is not available in Brigadier, but the no-arg form works with {@code MANA} only.
 */
public final class FabricManaCommands {

    private static final String ARG_PLAYER = "player";
    private static final String ARG_AMOUNT = "amount";

    private FabricManaCommands() {}

    public static LiteralArgumentBuilder<CommandSourceStack> manaCommandRoot() {
        return literal("mana")
                .executes(FabricManaCommands::executeSelf)
                .then(literal("view")
                        .executes(FabricManaCommands::executeSelf)
                        .then(Commands.argument(ARG_PLAYER, StringArgumentType.word())
                                .requires(
                                        FabricCommandRequirements.permission(
                                                FabricCommandPermissions.MANA_OTHER, false))
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

    private static RequiredArgumentBuilder<CommandSourceStack, String> playerAmount(
            com.mojang.brigadier.Command<CommandSourceStack> end) {
        return Commands.argument(ARG_PLAYER, StringArgumentType.word())
                .suggests(FabricCommandSuggestionProviders.onlinePlayers())
                .then(Commands.argument(ARG_AMOUNT, DoubleArgumentType.doubleArg(0.0)).executes(end));
    }

    private static int executeSelf(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        if (!Permissions.check(source, FabricCommandPermissions.MANA, true)) {
            source.sendFailure(
                    FabricGraniteText.key("graniteskills.granite.command.permission_denied"));
            return 0;
        }
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(p -> {
                    if (!(source.getEntity() instanceof ServerPlayer player)) {
                        source.sendFailure(
                                FabricGraniteText.key(
                                        "graniteskills.granite.command.mana.console_specify"));
                        return 0;
                    }
                    User user = p.getUserManager().getUser(player.getUUID());
                    if (user == null) {
                        source.sendFailure(Component.literal(
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
                    source.sendFailure(Component.literal("GraniteSkills is still starting."));
                    return 0;
                });
    }

    private static int executeView(CommandContext<CommandSourceStack> ctx) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(p -> {
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
                        source.sendFailure(Component.literal(
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
                    ctx.getSource().sendFailure(Component.literal("GraniteSkills is still starting."));
                    return 0;
                });
    }

    private static int executeSet(CommandContext<CommandSourceStack> ctx) {
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
                    ctx.getSource().sendFailure(Component.literal("GraniteSkills is still starting."));
                    return 0;
                });
    }

    @FunctionalInterface
    private interface TargetMutator {
        int apply(User user, ServerPlayer targetPlayer);
    }

    private static int mutateTarget(CommandContext<CommandSourceStack> ctx, FabricGraniteSkills p, TargetMutator m) {
        CommandSourceStack source = ctx.getSource();
        String name = StringArgumentType.getString(ctx, ARG_PLAYER);
        ServerPlayer target =
                FabricCommandPlayerTarget.findOnline(source.getServer(), name).orElse(null);
        if (target == null) {
            source.sendFailure(
                    FabricGraniteText.key("graniteskills.granite.command.xp.player_not_found", name));
            return 0;
        }
        User user = p.getUserManager().getUser(target.getUUID());
        if (user == null) {
            source.sendFailure(Component.literal(
                    p.getMsg(CommandMessage.NO_PROFILE, p.getDefaultLanguage())));
            return 0;
        }
        return m.apply(user, target);
    }
}
