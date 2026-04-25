package dev.aurelium.auraskills.fabric.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.aurelium.auraskills.common.message.type.CommandMessage;
import dev.aurelium.auraskills.common.util.text.TextUtil;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
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
 * Bounded **backup** admin commands (**Sprint 60**). Uses common {@link
 * dev.aurelium.auraskills.common.storage.backup.BackupProvider} — **create** only (no restore /
 * delete). Bukkit reference: {@code backup save} / {@code backup load}; Granite adds **list** and
 * **info**, and uses {@code create} + optional {@code save} alias for **create**.
 */
public final class FabricBackupCommands {

    private static final int MAX_LIST_ENTRIES = 35;
    private static final DecimalFormat KB = new DecimalFormat("#,##0.0");

    private FabricBackupCommands() {}

    public static LiteralArgumentBuilder<ServerCommandSource> backupLiteral() {
        return literal("backup")
                .requires(
                        FabricCommandRequirements.permission(
                                FabricCommandPermissions.BACKUP, false))
                .then(literal("create").executes(FabricBackupCommands::executeCreate))
                .then(literal("save").executes(FabricBackupCommands::executeCreate))
                .then(literal("list").executes(FabricBackupCommands::executeList))
                .then(literal("info").executes(FabricBackupCommands::executeInfo));
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
                            CompletableFuture<java.io.File> future =
                                    p.getBackupProvider().saveBackupAsync(true);
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
