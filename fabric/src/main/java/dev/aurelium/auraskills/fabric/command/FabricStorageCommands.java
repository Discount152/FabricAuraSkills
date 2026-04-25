package dev.aurelium.auraskills.fabric.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.aurelium.auraskills.common.config.Option;
import dev.aurelium.auraskills.common.storage.file.FileStorageProvider;
import dev.aurelium.auraskills.common.storage.sql.SqlStorageProvider;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.literal;

/**
 * Bounded **storage** admin commands (**Sprint 63**): read-only **info**, **save** (flush loaded
 * profiles), and a **reload** explainer (no hot-swap of {@link
 * dev.aurelium.auraskills.common.storage.StorageProvider}). Does not expose SQL credentials, JDBC
 * URLs, or full host paths in chat.
 */
public final class FabricStorageCommands {

    private FabricStorageCommands() {}

    public static LiteralArgumentBuilder<ServerCommandSource> storageLiteral() {
        return literal("storage")
                .then(
                        literal("info")
                                .requires(
                                        FabricCommandRequirements.permission(
                                                FabricCommandPermissions.STORAGE, false))
                                .executes(FabricStorageCommands::executeInfo))
                .then(
                        literal("save")
                                .requires(
                                        FabricCommandRequirements.permission(
                                                FabricCommandPermissions.STORAGE, false))
                                .executes(FabricStorageCommands::executeSave))
                .then(
                        literal("reload")
                                .requires(
                                        FabricCommandRequirements.permission(
                                                FabricCommandPermissions.STORAGE, false))
                                .executes(FabricStorageCommands::executeReload));
    }

    private static int executeInfo(CommandContext<ServerCommandSource> ctx) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            ServerCommandSource source = ctx.getSource();
                            String backend = describeBackend(p);
                            String shortImpl = p.getStorageProvider().getClass().getSimpleName();
                            String implFq = p.getStorageProvider().getClass().getName();
                            String dataRoot = p.getPluginFolder().getName();
                            int loaded = p.getUserManager().getUserMap().size();
                            String out =
                                    Text.translatable("auraskills.granite.command.storage.info_header")
                                            .getString()
                                            + "\n"
                                            + Text.translatable(
                                                            "auraskills.granite.command.storage"
                                                                    + ".info_backend",
                                                            backend,
                                                            shortImpl)
                                                    .getString()
                                            + "\n"
                                            + Text.translatable(
                                                            "auraskills.granite.command.storage"
                                                                    + ".info_fqcn",
                                                            implFq)
                                                    .getString()
                                            + "\n"
                                            + Text.translatable(
                                                            "auraskills.granite.command.storage"
                                                                    + ".info_data_root",
                                                            dataRoot)
                                                    .getString()
                                            + "\n"
                                            + Text.translatable(
                                                            "auraskills.granite.command.storage"
                                                                    + ".info_loaded",
                                                            loaded)
                                                    .getString()
                                            + "\n"
                                            + Text.translatable(
                                                            "auraskills.granite.command.storage"
                                                                    + ".info_secrets",
                                                            p.configBoolean(Option.SQL_ENABLED)
                                                                    ? "true"
                                                                    : "false")
                                                    .getString()
                                            + "\n"
                                            + Text.translatable(
                                                            "auraskills.granite.command.storage"
                                                                    + ".info_note")
                                                    .getString();
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

    /** Human-readable active backend; never includes JDBC or passwords. */
    private static String describeBackend(FabricAuraSkills p) {
        if (p.getStorageProvider() instanceof FileStorageProvider) {
            return "YAML (file / userdata)";
        }
        if (p.getStorageProvider() instanceof SqlStorageProvider) {
            return "SQL (pooled, MySQL-compatible)";
        }
        return "unknown";
    }

    private static int executeSave(CommandContext<ServerCommandSource> ctx) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            ServerCommandSource source = ctx.getSource();
                            try {
                                int n = p.saveAllUserProfiles();
                                source.sendFeedback(
                                        () ->
                                                Text.translatable(
                                                        "auraskills.granite.command.storage"
                                                                + ".save_done",
                                                        n),
                                        true);
                                return 1;
                            } catch (Exception e) {
                                p.logger().severe("Storage save command failed", e);
                                source.sendError(
                                        Text.translatable(
                                                "auraskills.granite.command.storage.save_failed",
                                                String.valueOf(e.getMessage())));
                                return 0;
                            }
                        })
                .orElseGet(
                        () -> {
                            ctx.getSource()
                                    .sendError(Text.literal("AuraSkills is still starting."));
                            return 0;
                        });
    }

    private static int executeReload(CommandContext<ServerCommandSource> ctx) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            ServerCommandSource source = ctx.getSource();
                            // Hot-swapping StorageProvider is not supported; do not call
                            // reloadFromDisk() here (that is /auraskills reload).
                            source.sendFeedback(
                                    () ->
                                            Text.translatable(
                                                    "auraskills.granite.command.storage"
                                                            + ".reload_info"),
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
}
