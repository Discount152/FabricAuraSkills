package dev.aurelium.auraskills.fabric.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.aurelium.auraskills.common.message.type.CommandMessage;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.common.user.UserState;
import dev.aurelium.auraskills.common.util.text.TextUtil;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.text.FabricGraniteText;
import dev.aurelium.auraskills.fabric.user.FabricUserDataTransfer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.UUID;

import static net.minecraft.server.command.CommandManager.literal;

/**
 * Bukkit {@code SkillsRootCommand#onTransfer} — copies {@link UserState} from one UUID to another
 * via {@link dev.aurelium.auraskills.common.storage.StorageProvider#loadState} / {@code
 * applyState} and/or {@link User#applyState}. **Sprint 58.**
 */
public final class FabricTransferCommands {

    private static final String ARG_FROM = "from";
    private static final String ARG_TO = "to";

    private FabricTransferCommands() {}

    public static LiteralArgumentBuilder<ServerCommandSource> transferLiteral() {
        return literal("transfer")
                .requires(
                        FabricCommandRequirements.permission(
                                FabricCommandPermissions.TRANSFER, false))
                .then(
                        CommandManager.argument(ARG_FROM, StringArgumentType.word())
                                .then(
                                        CommandManager.argument(ARG_TO, StringArgumentType.word())
                                                .executes(FabricTransferCommands::execute)));
    }

    private static int execute(CommandContext<ServerCommandSource> ctx) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            ServerCommandSource source = ctx.getSource();
                            UUID from;
                            UUID to;
                            try {
                                from = UUID.fromString(StringArgumentType.getString(ctx, ARG_FROM));
                                to = UUID.fromString(StringArgumentType.getString(ctx, ARG_TO));
                            } catch (IllegalArgumentException e) {
                                source.sendError(
                                        FabricGraniteText.key(
                                                "auraskills.granite.command.transfer.bad_uuid"));
                                return 0;
                            }
                            if (from.equals(to)) {
                                source.sendError(
                                        FabricGraniteText.key(
                                                "auraskills.granite.command.transfer.self"));
                                return 0;
                            }
                            Locale lo = senderLocale(p, source);
                            if (FabricUserDataTransfer.isFileStorage(p)
                                    && !FabricUserDataTransfer.isPlayerOnline(
                                            p.getServer(), from)
                                    && !FabricUserDataTransfer.hasYamlDataFile(p, from)) {
                                source.sendError(
                                        FabricGraniteText.key(
                                                "auraskills.granite.command.transfer.no_source_data",
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
                                    source.sendError(
                                            FabricGraniteText.key(
                                                    "auraskills.granite.command.transfer.source_not_loaded",
                                                    from.toString()));
                                    return 0;
                                }
                            } else {
                                try {
                                    state = p.getStorageProvider().loadState(from);
                                } catch (Exception e) {
                                    p.logger()
                                            .warn("Transfer: loadState failed for " + from, e);
                                    source.sendError(Text.literal(errorLine(p, lo)));
                                    return 0;
                                }
                            }
                            return applyWithState(p, source, from, to, state, lo);
                        })
                .orElseGet(
                        () -> {
                            ctx.getSource().sendError(Text.literal("AuraSkills is still starting."));
                            return 0;
                        });
    }

    private static int applyWithState(
            FabricAuraSkills p,
            ServerCommandSource source,
            UUID from,
            UUID to,
            UserState oldState,
            Locale lo) {
        UserState newState = oldState.withUuid(to);
        if (FabricUserDataTransfer.isPlayerOnline(p.getServer(), to)) {
            User toUser = p.getUserManager().getUser(to);
            if (toUser == null) {
                p.logger()
                        .warn(
                                "Transfer: target " + to + " is online but not in the user map.");
                source.sendError(
                        FabricGraniteText.key(
                                "auraskills.granite.command.transfer.target_not_loaded",
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
                source.sendError(Text.literal(errorLine(p, lo)));
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
        source.sendFeedback(() -> Text.literal(p.getPrefix(lo) + body), true);
        source.sendFeedback(
                () -> FabricGraniteText.key(
                        "auraskills.granite.command.transfer.source_unchanged", from.toString()),
                false);
        return 1;
    }

    private static String errorLine(FabricAuraSkills p, Locale lo) {
        return p.getMessageProvider()
                .applyFormatting(p.getMsg(CommandMessage.TRANSFER_ERROR, lo));
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
