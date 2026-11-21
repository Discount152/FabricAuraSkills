package dev.granite.graniteskills.fabric.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.granite.graniteskills.common.message.type.CommandMessage;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.common.user.UserState;
import dev.granite.graniteskills.common.util.text.TextUtil;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import dev.granite.graniteskills.fabric.text.FabricGraniteText;
import dev.granite.graniteskills.fabric.user.FabricUserDataTransfer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.UUID;

import static net.minecraft.commands.Commands.literal;

/**
 * Bukkit {@code SkillsRootCommand#onTransfer} — copies {@link UserState} from one UUID to another
 * via {@link dev.granite.graniteskills.common.storage.StorageProvider#loadState} / {@code
 * applyState} and/or {@link User#applyState}. **Sprint 58.**
 */
public final class FabricTransferCommands {

    private static final String ARG_FROM = "from";
    private static final String ARG_TO = "to";

    private FabricTransferCommands() {}

    public static LiteralArgumentBuilder<CommandSourceStack> transferLiteral() {
        return literal("transfer")
                .requires(
                        FabricCommandRequirements.permission(
                                FabricCommandPermissions.TRANSFER, false))
                .then(
                        Commands.argument(ARG_FROM, StringArgumentType.word())
                                .then(
                                        Commands.argument(ARG_TO, StringArgumentType.word())
                                                .executes(ctx -> FabricTransferCommands.execute(ctx, false))
                                                .then(literal("confirm")
                                                        .executes(ctx -> FabricTransferCommands.execute(ctx, true)))));
    }

    private static int execute(CommandContext<CommandSourceStack> ctx, boolean confirmed) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            CommandSourceStack source = ctx.getSource();
                            UUID from;
                            UUID to;
                            try {
                                from = UUID.fromString(StringArgumentType.getString(ctx, ARG_FROM));
                                to = UUID.fromString(StringArgumentType.getString(ctx, ARG_TO));
                            } catch (IllegalArgumentException e) {
                                source.sendFailure(
                                        FabricGraniteText.key(
                                                "graniteskills.granite.command.transfer.bad_uuid"));
                                return 0;
                            }
                            if (from.equals(to)) {
                                source.sendFailure(
                                        FabricGraniteText.key(
                                                "graniteskills.granite.command.transfer.self"));
                                return 0;
                            }
                            Locale lo = senderLocale(p, source);
                            if (FabricUserDataTransfer.isFileStorage(p)
                                    && !FabricUserDataTransfer.isPlayerOnline(
                                            p.getServer(), from)
                                    && !FabricUserDataTransfer.hasYamlDataFile(p, from)) {
                                source.sendFailure(
                                        FabricGraniteText.key(
                                                "graniteskills.granite.command.transfer.no_source_data",
                                                from.toString()));
                                return 0;
                            }
                            @Nullable UserState state;
                            if (FabricUserDataTransfer.isPlayerOnline(p.getServer(), from)) {
                                state = FabricUserDataTransfer.getStateForOnlineUser(p, from);
                                if (state == null) {
                                    p.logger()
                                            .warn(
                                                    "Transfer: no user map entry for online source "
                                                            + from);
                                    source.sendFailure(
                                            FabricGraniteText.key(
                                                    "graniteskills.granite.command.transfer.source_not_loaded",
                                                    from.toString()));
                                    return 0;
                                }
                            } else {
                                try {
                                    state = p.getStorageProvider().loadState(from);
                                } catch (Exception e) {
                                    p.logger()
                                            .warn("Transfer: loadState failed for " + from, e);
                                    source.sendFailure(Component.literal(errorLine(p, lo)));
                                    return 0;
                                }
                            }
                            return applyWithState(p, source, from, to, state, lo, confirmed);
                        })
                .orElseGet(
                        () -> {
                            ctx.getSource().sendFailure(Component.literal("GraniteSkills is still starting."));
                            return 0;
                        });
    }

    private static int applyWithState(
            FabricGraniteSkills p,
            CommandSourceStack source,
            UUID from,
            UUID to,
            UserState oldState,
            Locale lo,
            boolean confirmed) {
        if (!confirmed && destinationHasProgress(p, to)) {
            source.sendFailure(FabricGraniteText.key(
                    "graniteskills.granite.command.transfer.destination_has_progress", to.toString()));
            return 0;
        }
        UserState newState = oldState.withUuid(to);
        if (FabricUserDataTransfer.isPlayerOnline(p.getServer(), to)) {
            User toUser = p.getUserManager().getUser(to);
            if (toUser == null) {
                p.logger()
                        .warn(
                                "Transfer: target " + to + " is online but not in the user map.");
                source.sendFailure(
                        FabricGraniteText.key(
                                "graniteskills.granite.command.transfer.target_not_loaded",
                                to.toString()));
                return 0;
            }
            toUser.applyState(newState);
            FabricUserDataTransfer.saveUserAfterStateApply(p, toUser);
            p.getRewardManager().updatePermissions(toUser);
        } else {
            try {
                p.getStorageProvider().applyState(newState);
            } catch (Exception e) {
                p.logger().warn("Transfer: applyState failed for offline " + to, e);
                source.sendFailure(Component.literal(errorLine(p, lo)));
                return 0;
            }
        }
        String body =
                p.getMessageProvider()
                        .applyFormatting(
                                TextUtil.replace(
                                        p.getMsg(CommandMessage.TRANSFER_SUCCESS, lo),
                                        "{from}",
                                        from.toString(),
                                        "{to}",
                                        to.toString()));
        source.sendSuccess(() -> Component.literal(p.getPrefix(lo) + body), true);
        source.sendSuccess(
                () -> FabricGraniteText.key(
                        "graniteskills.granite.command.transfer.source_unchanged", from.toString()),
                false);
        return 1;
    }

    private static String errorLine(FabricGraniteSkills p, Locale lo) {
        return p.getMessageProvider()
                .applyFormatting(p.getMsg(CommandMessage.TRANSFER_ERROR, lo));
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

    private static boolean destinationHasProgress(FabricGraniteSkills plugin, UUID to) {
        if (FabricUserDataTransfer.isPlayerOnline(plugin.getServer(), to)) {
            User user = plugin.getUserManager().getUser(to);
            return user != null && user.hasProgressBeyondStart();
        }
        try {
            UserState state = plugin.getStorageProvider().loadState(to);
            int start = plugin.config().getStartLevel();
            for (Integer level : state.skillLevels().values()) {
                if (level != null && level > start) {
                    return true;
                }
            }
            for (Double xp : state.skillXp().values()) {
                if (xp != null && xp > 0.0) {
                    return true;
                }
            }
            return !state.statModifiers().isEmpty() || !state.traitModifiers().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
}
