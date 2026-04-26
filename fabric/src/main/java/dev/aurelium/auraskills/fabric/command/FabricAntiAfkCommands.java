package dev.aurelium.auraskills.fabric.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.aurelium.auraskills.common.message.type.CommandMessage;
import dev.aurelium.auraskills.common.user.AntiAfkLog;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.common.util.text.TextUtil;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.text.FabricGraniteText;
import dev.aurelium.auraskills.fabric.user.FabricUserDataRead;
import dev.aurelium.auraskills.fabric.user.FabricUserDataTransfer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static net.minecraft.server.command.CommandManager.literal;

/**
 * Bukkit {@link dev.aurelium.auraskills.bukkit.commands.AntiAfkCommand} — <strong>Sprint
 * 67.</strong> Only subcommand: {@code %skills_alias% antiafk logs …} (no top-level
 * {@code /antiafk}). The command is <strong>read-only</strong>: it does not modify {@link
 * dev.aurelium.auraskills.common.antiafk.AntiAfkManager} — it shows persisted + in-session
 * {@link AntiAfkLog} lines from storage and {@link User#getSessionAntiAfkLogs()}.
 * <p>
 * ACF {@code @Flags("other")}: a player in-game may not use their own name; console may target
 * anyone with a resolvable name.
 */
public final class FabricAntiAfkCommands {

    private static final String ARG_PLAYER = "player";
    private static final String ARG_PAGE = "page";
    private static final String ARG_PER_PAGE = "perPage";

    private FabricAntiAfkCommands() {}

    public static LiteralArgumentBuilder<ServerCommandSource> antiafkRoot() {
        return literal("antiafk")
                .then(
                        literal("logs")
                                .requires(
                                        FabricCommandRequirements.permission(
                                                FabricCommandPermissions.ANTIAFK_LOGS, false))
                                .then(
                                        CommandManager.argument(
                                                        ARG_PLAYER, StringArgumentType.word())
                                                .suggests(
                                                        FabricCommandSuggestionProviders
                                                                .onlinePlayers())
                                                .executes(ctx -> executeLogs(ctx, 1, 10))
                                                .then(
                                                        CommandManager.argument(
                                                                        ARG_PAGE, integer(1))
                                                                .executes(
                                                                        ctx ->
                                                                                executeLogs(
                                                                                        ctx,
                                                                                        getInteger(
                                                                                                ctx,
                                                                                                ARG_PAGE),
                                                                                        10))
                                                                .then(
                                                                        CommandManager.argument(
                                                                                        ARG_PER_PAGE,
                                                                                        integer(1))
                                                                                .executes(
                                                                                        ctx -> {
                                                                                            int p =
                                                                                                    getInteger(
                                                                                                            ctx,
                                                                                                            ARG_PAGE);
                                                                                            int pp =
                                                                                                    getInteger(
                                                                                                            ctx,
                                                                                                            ARG_PER_PAGE);
                                                                                            return executeLogs(
                                                                                                    ctx,
                                                                                                    p,
                                                                                                    pp);
                                                                                        })))));

    }

    private static int executeLogs(
            CommandContext<ServerCommandSource> ctx, int page, int perPage) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            ServerCommandSource source = ctx.getSource();
                            String name = StringArgumentType.getString(ctx, ARG_PLAYER);
                            return dispatchLogs(p, source, name, page, perPage);
                        })
                .orElseGet(
                        () -> {
                            ctx.getSource()
                                    .sendError(
                                            Text.literal("AuraSkills is still starting."));
                            return 0;
                        });
    }

    private static int dispatchLogs(
            FabricAuraSkills p,
            ServerCommandSource source,
            String nameArg,
            int page,
            int perPage) {
        if (nameArg == null || nameArg.isEmpty()) {
            return 0;
        }
        var uuidOpt = FabricUserDataRead.resolveUuid(source.getServer(), nameArg);
        if (uuidOpt.isEmpty()) {
            source.sendError(
                    FabricGraniteText.key(
                            "auraskills.granite.command.profile.player_not_found", nameArg));
            return 0;
        }
        UUID uuid = uuidOpt.get();
        if (source.getEntity() instanceof ServerPlayerEntity sp
                && sp.getUuid().equals(uuid)) {
            source.sendError(
                    FabricGraniteText.key("auraskills.granite.command.antiafk.other_target_only"));
            return 0;
        }
        int offset = (page - 1) * perPage;
        var server = source.getServer();
        String resolved = FabricCommandLeaderboardText.resolvePlayerName(server, uuid);
        final String displayName = resolved.equals("?") ? nameArg : resolved;
        if (FabricUserDataTransfer.isPlayerOnline(server, uuid)) {
            User user = p.getUserManager().getUser(uuid);
            if (user == null) {
                source.sendError(
                        FabricGraniteText.key("auraskills.granite.command.profile.source_not_loaded", uuid));
                return 0;
            }
            if (user.getStoredAntiAfkLogs().isEmpty()) {
                p.getScheduler()
                        .executeAsync(
                                () -> {
                                    try {
                                        List<AntiAfkLog> loaded =
                                                p.getStorageProvider()
                                                        .loadAntiAfkLogs(uuid);
                                        user.setStoredAntiAfkLogs(
                                                new ArrayList<>(loaded));
                                        loaded.addAll(user.getSessionAntiAfkLogs());
                                        List<AntiAfkLog> slice =
                                                getMostRecent(loaded, offset, perPage);
                                        p.getScheduler()
                                                .executeSync(
                                                        () ->
                                                                sendLogsText(
                                                                        p,
                                                                        source,
                                                                        displayName,
                                                                        slice,
                                                                        page,
                                                                        perPage,
                                                                        loaded.size()));
                                    } catch (Exception e) {
                                        p.logger().severe("antiafk logs load failed", e);
                                        p.getScheduler()
                                                .executeSync(
                                                        () ->
                                                                source.sendError(
                                                                        FabricGraniteText.key(
                                                                                "auraskills.granite.command.antiafk.load_failed")));

                                    }
                                });
                return 1;
            } else {
                // Merge like Bukkit, without mutating User's internal stored list
                // (Bukkit's get() + addAll mutates the cache; a copy is equivalent for output).
                List<AntiAfkLog> all =
                        new ArrayList<>(
                                user.getStoredAntiAfkLogs()
                                        .orElseGet(ArrayList::new));
                all.addAll(user.getSessionAntiAfkLogs());
                List<AntiAfkLog> slice = getMostRecent(all, offset, perPage);
                sendLogsText(p, source, displayName, slice, page, perPage, all.size());
                return 1;
            }
        } else {
            p.getScheduler()
                    .executeAsync(
                            () -> {
                                try {
                                    List<AntiAfkLog> loaded =
                                            p.getStorageProvider().loadAntiAfkLogs(uuid);
                                    List<AntiAfkLog> slice = getMostRecent(loaded, offset, perPage);
                                    p.getScheduler()
                                            .executeSync(
                                                    () ->
                                                            sendLogsText(
                                                                    p,
                                                                    source,
                                                                    displayName,
                                                                    slice,
                                                                    page,
                                                                    perPage,
                                                                    loaded.size()));
                                } catch (Exception e) {
                                    p.logger().severe("antiafk logs load failed", e);
                                    p.getScheduler()
                                            .executeSync(
                                                    () ->
                                                            source.sendError(
                                                                    FabricGraniteText.key(
                                                                            "auraskills.granite.command.antiafk.load_failed")));

                                }
                            });
            return 1;
        }
    }

    private static void sendLogsText(
            FabricAuraSkills p,
            ServerCommandSource recipient,
            String targetName,
            List<AntiAfkLog> logs,
            int page,
            int perPage,
            int total) {
        Locale locale = senderLocale(p, recipient);
        int pages = (total - 1) / perPage + 1;
        var sb = new StringBuilder();
        sb.append(
                TextUtil.replace(
                        p.getMsg(CommandMessage.ANTIAFK_LOGS_HEADER, locale),
                        "{player}",
                        targetName,
                        "{count}",
                        String.valueOf(total),
                        "{page}",
                        String.valueOf(page),
                        "{total_pages}",
                        String.valueOf(pages)));
        sb.append("\n");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss");
        for (AntiAfkLog log : logs) {
            String formattedTime =
                    Instant.ofEpochMilli(log.timestamp())
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime()
                            .format(formatter);
            sb.append(
                    TextUtil.replace(
                            p.getMsg(CommandMessage.ANTIAFK_LOGS_ENTRY, locale),
                            "{timestamp}",
                            formattedTime,
                            "{message}",
                            log.message()));
            sb.append("\n");
        }
        if (sb.length() >= 2) {
            sb.delete(sb.length() - 2, sb.length());
        }
        String out = sb.toString();
        recipient.sendFeedback(() -> Text.literal(out), false);
    }

    private static List<AntiAfkLog> getMostRecent(
            List<AntiAfkLog> logs, int offset, int limit) {
        List<AntiAfkLog> copied = new ArrayList<>(logs);
        copied.sort(Comparator.comparingLong(AntiAfkLog::timestamp).reversed());
        List<AntiAfkLog> limited = new ArrayList<>();
        for (int i = offset; i < offset + limit && i < copied.size(); i++) {
            limited.add(copied.get(i));
        }
        return limited;
    }

    private static Locale senderLocale(
            FabricAuraSkills p, ServerCommandSource source) {
        if (source.getEntity() instanceof ServerPlayerEntity sp) {
            @Nullable User u = p.getUserManager().getUser(sp.getUuid());
            if (u != null) {
                return u.getLocale();
            }
        }
        return p.getDefaultLanguage();
    }
}
