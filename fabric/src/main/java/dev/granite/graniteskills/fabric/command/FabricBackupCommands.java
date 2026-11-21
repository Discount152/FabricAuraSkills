package dev.granite.graniteskills.fabric.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.granite.graniteskills.common.message.type.CommandMessage;
import dev.granite.graniteskills.common.util.text.TextUtil;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import dev.granite.graniteskills.fabric.text.FabricGraniteText;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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

import static net.minecraft.commands.Commands.literal;

/**
 * Bounded **backup** admin commands: **Sprint 60** (create, list, info) and **Sprint 61** (guarded
 * **load** / **restore** with confirmation, safe paths, pre-restore snapshot). Uses common {@link
 * dev.granite.graniteskills.common.storage.backup.BackupProvider}.
 */
public final class FabricBackupCommands {

    private static final int MAX_LIST_ENTRIES = 35;
    private static final DecimalFormat KB = new DecimalFormat("#,##0.0");
    private static final String ARG_FILE = "file";

    private FabricBackupCommands() {}

    public static LiteralArgumentBuilder<CommandSourceStack> backupLiteral() {
        return literal("backup")
                .requires(FabricCommandRequirements.backupFamilyAccess())
                .executes(FabricBackupCommands::executeRootUsage)
                .then(
                        literal("create")
                                .requires(
                                        FabricCommandRequirements.permissionOrNonPlayerSource(
                                                FabricCommandPermissions.BACKUP, false))
                                .executes(FabricBackupCommands::executeCreate))
                .then(
                        literal("save")
                                .requires(
                                        FabricCommandRequirements.permissionOrNonPlayerSource(
                                                FabricCommandPermissions.BACKUP, false))
                                .executes(FabricBackupCommands::executeCreate))
                .then(
                        literal("list")
                                .requires(
                                        FabricCommandRequirements.permissionOrNonPlayerSource(
                                                FabricCommandPermissions.BACKUP, false))
                                .executes(FabricBackupCommands::executeList))
                .then(
                        literal("info")
                                .requires(
                                        FabricCommandRequirements.permissionOrNonPlayerSource(
                                                FabricCommandPermissions.BACKUP, false))
                                .executes(FabricBackupCommands::executeInfo))
                .then(backupLoadSubtree("load"))
                .then(backupLoadSubtree("restore"));
    }

    private static int executeRootUsage(CommandContext<CommandSourceStack> ctx) {
        ctx.getSource()
                .sendSuccess(
                        () -> FabricGraniteText.key("graniteskills.granite.command.backup.usage_root"),
                        false);
        return 1;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> backupLoadSubtree(String name) {
        SuggestionProvider<CommandSourceStack> yml = FabricCommandSuggestionProviders.backupYmlInBackupsFolder();
        return literal(name)
                .requires(
                        FabricCommandRequirements.permissionOrNonPlayerSource(
                                FabricCommandPermissions.BACKUP_LOAD, false))
                .then(
                        Commands.argument(ARG_FILE, StringArgumentType.word())
                                .suggests(yml)
                                .executes(FabricBackupCommands::executeLoadPreview)
                                .then(
                                        literal("confirm")
                                                .executes(FabricBackupCommands::executeLoadConfirm))
                                .then(
                                        literal("cancel")
                                                .executes(FabricBackupCommands::executeLoadCancel)));
    }

    private static int executeCreate(CommandContext<CommandSourceStack> ctx) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            CommandSourceStack source = ctx.getSource();
                            Locale lo = localeForSource(source, p);
                            String saving =
                                    p.getMessageProvider()
                                            .applyFormatting(
                                                    p.getMsg(CommandMessage.BACKUP_SAVE_SAVING, lo));
                            String prefix = p.getPrefix(lo);
                            source.sendSuccess(() -> Component.literal(prefix + saving), true);
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
                                    .sendFailure(Component.literal("GraniteSkills is still starting."));
                            return 0;
                        });
    }

    private static void onCreateComplete(
            FabricGraniteSkills p,
            CommandSourceStack source,
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
            source.sendFailure(Component.literal(err));
            return;
        }
        if (file == null) {
            source.sendFailure(
                    FabricGraniteText.key("graniteskills.granite.command.backup.skipped_too_many_users"));
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
        source.sendSuccess(() -> Component.literal(out), false);
    }

    private static int executeList(CommandContext<CommandSourceStack> ctx) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            CommandSourceStack source = ctx.getSource();
                            p.getBackupProvider().createBackupFolder();
                            File dir = new File(p.getPluginFolder(), "backups");
                            if (!dir.isDirectory()) {
                                source.sendSuccess(
                                        () ->
                                                FabricGraniteText.key(
                                                        "graniteskills.granite.command.backup.list_empty"),
                                        false);
                                return 1;
                            }
                            File[] raw = dir.listFiles((d, name) -> name.endsWith(".yml"));
                            if (raw == null || raw.length == 0) {
                                source.sendSuccess(
                                        () ->
                                                FabricGraniteText.key(
                                                        "graniteskills.granite.command.backup.list_empty"),
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
                                    FabricGraniteText.key(
                                                    "graniteskills.granite.command.backup.list_header",
                                                    total)
                                            .getString();
                            StringBuilder body = new StringBuilder(header).append("\n");
                            for (String line : lines) {
                                body.append(line).append("\n");
                            }
                            if (total > show) {
                                body.append(
                                        FabricGraniteText.key(
                                                        "graniteskills.granite.command.backup.list_truncated",
                                                        total - show)
                                                .getString());
                            }
                            String out = body.toString();
                            source.sendSuccess(() -> Component.literal(out), false);
                            return 1;
                        })
                .orElseGet(
                        () -> {
                            ctx.getSource()
                                    .sendFailure(Component.literal("GraniteSkills is still starting."));
                            return 0;
                        });
    }

    private static int executeInfo(CommandContext<CommandSourceStack> ctx) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            ctx.getSource()
                                    .sendSuccess(
                                            () ->
                                                    FabricGraniteText.key(
                                                            "graniteskills.granite.command.backup.info_body"),
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

    private static int executeLoadPreview(CommandContext<CommandSourceStack> ctx) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            CommandSourceStack source = ctx.getSource();
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
                                source.sendFailure(Component.literal(p.getPrefix(lo) + nf));
                                return 0;
                            }
                            String confirm =
                                    p.getMessageProvider()
                                            .applyFormatting(
                                                    p.getMsg(CommandMessage.BACKUP_LOAD_CONFIRM, lo));
                            String extra =
                                    FabricGraniteText.key(
                                                    "graniteskills.granite.command.backup.load_preview_run_confirm",
                                                    file.getName())
                                            .getString();
                            String out = p.getPrefix(lo) + confirm + "\n" + extra;
                            source.sendSuccess(() -> Component.literal(out), false);
                            FabricCommandConfirmation.registerPending(
                                    p, source, file.getName());
                            return 1;
                        })
                .orElseGet(
                        () -> {
                            ctx.getSource()
                                    .sendFailure(Component.literal("GraniteSkills is still starting."));
                            return 0;
                        });
    }

    private static int executeLoadCancel(CommandContext<CommandSourceStack> ctx) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            CommandSourceStack source = ctx.getSource();
                            Locale lo = localeForSource(source, p);
                            String nameArg = StringArgumentType.getString(ctx, ARG_FILE);
                            File target = resolveForLoad(p, nameArg, source, lo);
                            if (target == null) {
                                return 0;
                            }
                            if (!FabricCommandConfirmation.cancelPending(source, target.getName())) {
                                return 0;
                            }
                            p.logger().info("Backup load cancelled file=" + target.getName());
                            source.sendSuccess(
                                    () ->
                                            Component.literal(
                                                    "Backup load cancelled for " + target.getName()),
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

    private static int executeLoadConfirm(CommandContext<CommandSourceStack> ctx) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            CommandSourceStack source = ctx.getSource();
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
                                source.sendFailure(Component.literal(p.getPrefix(lo) + nf));
                                return 0;
                            }
                            if (!FabricCommandConfirmation.verifyPendingOrError(
                                    source, target.getName())) {
                                p.logger().info("Backup load confirm rejected file=" + target.getName());
                                return 0;
                            }
                            String loading =
                                    p.getMessageProvider()
                                            .applyFormatting(
                                                    p.getMsg(CommandMessage.BACKUP_LOAD_LOADING, lo));
                            source.sendSuccess(() -> Component.literal(p.getPrefix(lo) + loading), true);
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
                                    .sendFailure(Component.literal("GraniteSkills is still starting."));
                            return 0;
                        });
    }

    private static void runPreRestoreAndLoad(
            FabricGraniteSkills p, CommandSourceStack source, Locale lo, File target) {
        try {
            File pre = p.getBackupProvider().saveBackupSync(true);
            if (pre == null) {
                p.getScheduler()
                        .executeSync(
                                () ->
                                        source.sendFailure(
                                                FabricGraniteText.key(
                                                        "graniteskills.granite.command.backup"
                                                                + ".prerestore_skipped")));
                return;
            }
        } catch (Exception e) {
            p.logger().severe("Pre-restore backup failed", e);
            p.getScheduler()
                    .executeSync(
                            () ->
                                    source.sendFailure(
                                            FabricGraniteText.key(
                                                    "graniteskills.granite.command.backup.prerestore_failed")));
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
                                                    p.logger()
                                                            .info(
                                                                    "Backup load applied file="
                                                                            + target.getName()
                                                                            + " online refreshed");
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
                                                    source.sendSuccess(
                                                            () -> Component.literal(out), false);
                                                    String caveat =
                                                            FabricGraniteText.key(
                                                                            "graniteskills.granite.command"
                                                                                    + ".backup.online_refreshed_note")
                                                                    .getString();
                                                    source.sendSuccess(
                                                            () -> Component.literal(caveat), false);
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
                                                source.sendFailure(Component.literal(
                                                        p.getPrefix(lo) + err));
                                            });
                        });
    }

    private static @Nullable File resolveForLoad(
            FabricGraniteSkills p, String nameArg, CommandSourceStack source, Locale lo) {
        if (FabricBackupPathValidator.validateFileNameToken(nameArg).isPresent()) {
            source.sendFailure(
                    FabricGraniteText.key("graniteskills.granite.command.backup.unsafe_filename"));
            return null;
        }
        File f = FabricBackupPathValidator.resolveSafeBackupFile(p.getPluginFolder(), nameArg);
        if (f == null) {
            source.sendFailure(
                    FabricGraniteText.key(
                            "graniteskills.granite.command.backup.path_not_in_backups_or_no_folder"));
            return null;
        }
        return f;
    }

    private static Locale localeForSource(CommandSourceStack source, FabricGraniteSkills p) {
        if (source.getEntity() instanceof ServerPlayer sp) {
            var u = p.getUserManager().getUser(sp.getUUID());
            if (u != null) {
                return u.getLocale();
            }
        }
        return p.getDefaultLanguage();
    }
}
