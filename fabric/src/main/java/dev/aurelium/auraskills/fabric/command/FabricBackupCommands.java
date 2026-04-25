package dev.aurelium.auraskills.fabric.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.aurelium.auraskills.common.message.type.CommandMessage;
import dev.aurelium.auraskills.common.util.text.TextUtil;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.server.command.CommandManager.literal;

/**
 * Bounded **backup** admin commands: **Sprint 60** (create, list, info) and **Sprint 61** (guarded
 * **load** / **restore** with confirmation, safe paths, pre-restore snapshot). Uses common {@link
 * dev.aurelium.auraskills.common.storage.backup.BackupProvider}.
 */
public final class FabricBackupCommands {

    private static final int MAX_LIST_ENTRIES = 35;
    private static final DecimalFormat KB = new DecimalFormat("#,##0.0");
    private static final String ARG_FILE = "file";

    private FabricBackupCommands() {}

    public static LiteralArgumentBuilder<ServerCommandSource> backupLiteral() {
        return literal("backup")
                .then(
                        literal("create")
                                .requires(
                                        FabricCommandRequirements.permission(
                                                FabricCommandPermissions.BACKUP, false))
                                .executes(FabricBackupCommands::executeCreate))
                .then(
                        literal("save")
                                .requires(
                                        FabricCommandRequirements.permission(
                                                FabricCommandPermissions.BACKUP, false))
                                .executes(FabricBackupCommands::executeCreate))
                .then(
                        literal("list")
                                .requires(
                                        FabricCommandRequirements.permission(
                                                FabricCommandPermissions.BACKUP, false))
                                .executes(FabricBackupCommands::executeList))
                .then(
                        literal("info")
                                .requires(
                                        FabricCommandRequirements.permission(
                                                FabricCommandPermissions.BACKUP, false))
                                .executes(FabricBackupCommands::executeInfo))
                .then(backupLoadSubtree("load"))
                .then(backupLoadSubtree("restore"));
    }

    private static LiteralArgumentBuilder<ServerCommandSource> backupLoadSubtree(String name) {
        SuggestionProvider<ServerCommandSource> yml = FabricCommandSuggestionProviders.backupYmlInBackupsFolder();
        return literal(name)
                .requires(
                        FabricCommandRequirements.permission(
                                FabricCommandPermissions.BACKUP_LOAD, false))
                .then(
                        CommandManager.argument(ARG_FILE, StringArgumentType.word())
                                .suggests(yml)
                                .executes(FabricBackupCommands::executeLoadPreview)
                                .then(
                                        literal("confirm")
                                                .executes(FabricBackupCommands::executeLoadConfirm)));
    }

    private static int executeCreate(CommandContext<ServerCommandSource> ctx) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            ServerCommandSource source = ctx.getSource();
                            Locale lo = localeForSource(source, p);
                            String saving =
                                    p.getMessageProvider()
                                            .applyFormatting(
                                                    p.getMsg(CommandMessage.BACKUP_SAVE_SAVING, lo));
                            String prefix = p.getPrefix(lo);
                            source.sendFeedback(() -> Text.literal(prefix + saving), true);
                            CompletableFuture<File> future = p.getBackupProvider().saveBackupAsync(true);
                            future.whenComplete(
                                    (file, ex) ->
                                            p.getScheduler()
                                                    .executeSync(
                                                            () ->
                                                                    onCreateComplete(
                                                                            p,
                                                                            source,
                                                                            lo,
                                                                            file,
                                                                            ex)));
                            return 1;
                        })
                .orElseGet(
                        () -> {
                            ctx.getSource()
                                    .sendError(Text.literal("AuraSkills is still starting."));
                            return 0;
                        });
    }

    private static void onCreateComplete(
            FabricAuraSkills p,
            ServerCommandSource source,
            Locale lo,
            @Nullable File file,
            @Nullable Throwable ex) {
        if (ex != null) {
            p.logger().severe("Backup create failed", ex);
            String err =
                    p.getMessageProvider()
                            .applyFormatting(
                                    TextUtil.replace(
                                            p.getMsg(CommandMessage.BACKUP_SAVE_ERROR, lo),
                                            "{type}",
                                            p.getStorageProvider().getClass().getSimpleName()));
            source.sendError(Text.literal(err));
            return;
        }
        if (file == null) {
            source.sendError(
                    Text.translatable("auraskills.granite.command.backup.skipped_too_many_users"));
            return;
        }
        String line =
                p.getMessageProvider()
                        .applyFormatting(
                                TextUtil.replace(
                                        p.getMsg(CommandMessage.BACKUP_SAVE_SAVED, lo),
                                        "{type}",
                                        p.getStorageProvider().getClass().getSimpleName(),
                                        "{file}",
                                        file.getName()));
        String out = p.getPrefix(lo) + line;
        source.sendFeedback(() -> Text.literal(out), false);
    }

    private static int executeList(CommandContext<ServerCommandSource> ctx) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            ServerCommandSource source = ctx.getSource();
                            p.getBackupProvider().createBackupFolder();
                            File dir = new File(p.getPluginFolder(), "backups");
                            if (!dir.isDirectory()) {
                                source.sendFeedback(
                                        () ->
                                                Text.translatable(
                                                        "auraskills.granite.command.backup.list_empty"),
                                        false);
                                return 1;
                            }
                            File[] raw = dir.listFiles((d, name) -> name.endsWith(".yml"));
                            if (raw == null || raw.length == 0) {
                                source.sendFeedback(
                                        () ->
                                                Text.translatable(
                                                        "auraskills.granite.command.backup.list_empty"),
                                        false);
                                return 1;
                            }
                            Arrays.sort(
                                    raw,
                                    Comparator.comparingLong(File::lastModified).reversed());
                            int total = raw.length;
                            int show = Math.min(total, MAX_LIST_ENTRIES);
                            DateTimeFormatter fmt =
                                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                                            .withZone(ZoneId.systemDefault());
                            List<String> lines = new ArrayList<>();
                            for (int i = 0; i < show; i++) {
                                File f = raw[i];
                                String when =
                                        fmt.format(
                                                Instant.ofEpochMilli(f.lastModified())
                                                        .atZone(ZoneId.systemDefault()));
                                String sizeKb = KB.format(f.length() / 1024.0) + " KiB";
                                lines.add(
                                        String.format(
                                                "%d) %s  |  %s  |  %s",
                                                i + 1, f.getName(), sizeKb, when));
                            }
                            String header =
                                    Text.translatable(
                                                    "auraskills.granite.command.backup.list_header",
                                                    total)
                                            .getString();
                            StringBuilder body = new StringBuilder(header).append("\n");
                            for (String line : lines) {
                                body.append(line).append("\n");
                            }
                            if (total > show) {
                                body.append(
                                        Text.translatable(
                                                        "auraskills.granite.command.backup.list_truncated",
                                                        total - show)
                                                .getString());
                            }
                            String out = body.toString();
                            source.sendFeedback(() -> Text.literal(out), false);
                            return 1;
                        })
                .orElseGet(
                        () -> {
                            ctx.getSource()
                                    .sendError(Text.literal("AuraSkills is still starting."));
                            return 0;
                        });
    }

    private static int executeInfo(CommandContext<ServerCommandSource> ctx) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            ctx.getSource()
                                    .sendFeedback(
                                            () ->
                                                    Text.translatable(
                                                            "auraskills.granite.command.backup.info_body"),
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

    private static int executeLoadPreview(CommandContext<ServerCommandSource> ctx) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            ServerCommandSource source = ctx.getSource();
                            Locale lo = localeForSource(source, p);
                            String nameArg = StringArgumentType.getString(ctx, ARG_FILE);
                            File file = resolveForLoad(p, nameArg, source, lo);
                            if (file == null) {
                                return 0;
                            }
                            if (!file.isFile()) {
                                String nf =
                                        p.getMessageProvider()
                                                .applyFormatting(
                                                        p.getMsg(
                                                                CommandMessage
                                                                        .BACKUP_LOAD_FILE_NOT_FOUND,
                                                                lo));
                                source.sendError(Text.literal(p.getPrefix(lo) + nf));
                                return 0;
                            }
                            String confirm =
                                    p.getMessageProvider()
                                            .applyFormatting(
                                                    p.getMsg(CommandMessage.BACKUP_LOAD_CONFIRM, lo));
                            String extra =
                                    Text.translatable(
                                                    "auraskills.granite.command.backup.load_preview_run_confirm",
                                                    file.getName())
                                            .getString();
                            String out = p.getPrefix(lo) + confirm + "\n" + extra;
                            source.sendFeedback(() -> Text.literal(out), false);
                            return 1;
                        })
                .orElseGet(
                        () -> {
                            ctx.getSource()
                                    .sendError(Text.literal("AuraSkills is still starting."));
                            return 0;
                        });
    }

    private static int executeLoadConfirm(CommandContext<ServerCommandSource> ctx) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            ServerCommandSource source = ctx.getSource();
                            Locale lo = localeForSource(source, p);
                            String nameArg = StringArgumentType.getString(ctx, ARG_FILE);
                            File target = resolveForLoad(p, nameArg, source, lo);
                            if (target == null) {
                                return 0;
                            }
                            if (!target.isFile()) {
                                String nf =
                                        p.getMessageProvider()
                                                .applyFormatting(
                                                        p.getMsg(
                                                                CommandMessage
                                                                        .BACKUP_LOAD_FILE_NOT_FOUND,
                                                                lo));
                                source.sendError(Text.literal(p.getPrefix(lo) + nf));
                                return 0;
                            }
                            String loading =
                                    p.getMessageProvider()
                                            .applyFormatting(
                                                    p.getMsg(CommandMessage.BACKUP_LOAD_LOADING, lo));
                            source.sendFeedback(() -> Text.literal(p.getPrefix(lo) + loading), true);
                            p.getScheduler()
                                    .executeAsync(
                                            () ->
                                                    runPreRestoreAndLoad(
                                                            p, source, lo, target));
                            return 1;
                        })
                .orElseGet(
                        () -> {
                            ctx.getSource()
                                    .sendError(Text.literal("AuraSkills is still starting."));
                            return 0;
                        });
    }

    private static void runPreRestoreAndLoad(
            FabricAuraSkills p, ServerCommandSource source, Locale lo, File target) {
        try {
            File pre = p.getBackupProvider().saveBackupSync(true);
            if (pre == null) {
                p.getScheduler()
                        .executeSync(
                                () ->
                                        source.sendError(
                                                Text.translatable(
                                                        "auraskills.granite.command.backup"
                                                                + ".prerestore_skipped")));
                return;
            }
        } catch (Exception e) {
            p.logger().severe("Pre-restore backup failed", e);
            p.getScheduler()
                    .executeSync(
                            () ->
                                    source.sendError(
                                            Text.translatable(
                                                    "auraskills.granite.command.backup.prerestore_failed")));
            return;
        }
        long start = System.currentTimeMillis();
        p.getBackupProvider()
                .loadBackupAsync(
                        target,
                        () ->
                                p.getScheduler()
                                        .executeSync(
                                                () -> {
                                                    long ms = System.currentTimeMillis() - start;
                                                    FabricCommandStorageUtil.refreshOnlineUsersFromStorage(
                                                            p);
                                                    String line =
                                                            p.getMessageProvider()
                                                                    .applyFormatting(
                                                                            p.getMsg(
                                                                                    CommandMessage
                                                                                            .BACKUP_LOAD_LOADED,
                                                                                    lo));
                                                    String out =
                                                            p.getPrefix(lo)
                                                                    + line
                                                                    + " ("
                                                                    + ms
                                                                    + " ms)";
                                                    source.sendFeedback(
                                                            () -> Text.literal(out), false);
                                                    String caveat =
                                                            Text.translatable(
                                                                            "auraskills.granite.command"
                                                                                    + ".backup.online_refreshed_note")
                                                                    .getString();
                                                    source.sendFeedback(
                                                            () -> Text.literal(caveat), false);
                                                }),
                        e -> {
                            p.logger().severe("Backup load failed", e);
                            p.getScheduler()
                                    .executeSync(
                                            () -> {
                                                String err =
                                                        p.getMessageProvider()
                                                                .applyFormatting(
                                                                        TextUtil.replace(
                                                                                p.getMsg(
                                                                                        CommandMessage
                                                                                                .BACKUP_LOAD_ERROR,
                                                                                        lo),
                                                                                "{error}",
                                                                                String.valueOf(
                                                                                        e.getMessage())));
                                                source.sendError(Text.literal(
                                                        p.getPrefix(lo) + err));
                                            });
                        });
    }

    private static @Nullable File resolveForLoad(
            FabricAuraSkills p, String nameArg, ServerCommandSource source, Locale lo) {
        if (FabricBackupPathValidator.validateFileNameToken(nameArg).isPresent()) {
            source.sendError(
                    Text.translatable("auraskills.granite.command.backup.unsafe_filename"));
            return null;
        }
        File f = FabricBackupPathValidator.resolveSafeBackupFile(p.getPluginFolder(), nameArg);
        if (f == null) {
            source.sendError(
                    Text.translatable(
                            "auraskills.granite.command.backup.path_not_in_backups_or_no_folder"));
            return null;
        }
        return f;
    }

    private static Locale localeForSource(ServerCommandSource source, FabricAuraSkills p) {
        if (source.getEntity() instanceof ServerPlayerEntity sp) {
            var u = p.getUserManager().getUser(sp.getUuid());
            if (u != null) {
                return u.getLocale();
            }
        }
        return p.getDefaultLanguage();
    }
}
