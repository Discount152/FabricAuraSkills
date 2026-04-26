package dev.aurelium.auraskills.fabric.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.aurelium.auraskills.common.config.preset.ConfigPreset;
import dev.aurelium.auraskills.common.config.preset.PresetEntry;
import dev.aurelium.auraskills.common.config.preset.PresetLoadException;
import dev.aurelium.auraskills.common.config.preset.PresetLoadResult;
import dev.aurelium.auraskills.common.message.MessageKey;
import dev.aurelium.auraskills.common.message.type.CommandMessage;
import dev.aurelium.auraskills.common.util.text.TextUtil;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.text.FabricGraniteText;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static net.minecraft.server.command.CommandManager.literal;

/**
 * Bounded **preset** commands (**Sprint 62**): list / info (read-only), guarded **load** with **confirm**
 * and pre-apply UserState backup via {@link dev.aurelium.auraskills.common.storage.backup.BackupProvider}.
 * Uses common {@link dev.aurelium.auraskills.common.config.preset.PresetManager} (zip + {@code preset.yml}).
 */
public final class FabricPresetCommands {

    private static final String ARG_NAME = "name";
    private static final int MAX_LIST = 30;

    private FabricPresetCommands() {}

    public static LiteralArgumentBuilder<ServerCommandSource> presetLiteral() {
        SuggestionProvider<ServerCommandSource> zips =
                FabricCommandSuggestionProviders.presetZipsInPresetsFolder();
        return literal("preset")
                .requires(
                        FabricCommandRequirements.permissionOrNonPlayerSource(
                                FabricCommandPermissions.PRESET, false))
                .executes(FabricPresetCommands::executeRootUsage)
                .then(
                        literal("list")
                                .requires(
                                        FabricCommandRequirements.permissionOrNonPlayerSource(
                                                FabricCommandPermissions.PRESET, false))
                                .executes(FabricPresetCommands::executeList))
                .then(
                        literal("info")
                                .requires(
                                        FabricCommandRequirements.permissionOrNonPlayerSource(
                                                FabricCommandPermissions.PRESET, false))
                                .then(
                                        CommandManager.argument(ARG_NAME, StringArgumentType.word())
                                                .suggests(zips)
                                                .executes(FabricPresetCommands::executeInfo)))
                .then(
                        literal("load")
                                .requires(
                                        FabricCommandRequirements.permissionOrNonPlayerSource(
                                                FabricCommandPermissions.PRESET, false))
                                .then(
                                        CommandManager.argument(ARG_NAME, StringArgumentType.word())
                                                .suggests(zips)
                                                .executes(FabricPresetCommands::executeLoadPreview)
                                                .then(
                                                        literal("confirm")
                                                                .executes(
                                                                        FabricPresetCommands
                                                                                ::executeLoadConfirm))));
    }

    private static int executeRootUsage(CommandContext<ServerCommandSource> ctx) {
        ctx.getSource()
                .sendFeedback(
                        () -> FabricGraniteText.key("auraskills.granite.command.preset.usage_root"),
                        false);
        return 1;
    }

    private static int executeList(CommandContext<ServerCommandSource> ctx) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            ServerCommandSource source = ctx.getSource();
                            List<String> names = discoverPresetZipNames(p.getPluginFolder());
                            if (names.isEmpty()) {
                                source.sendFeedback(
                                        () ->
                                                Text.translatable(
                                                        "auraskills.granite.command.preset.list_empty"),
                                        false);
                                return 1;
                            }
                            int total = names.size();
                            boolean truncated = total > MAX_LIST;
                            List<String> show = truncated ? names.subList(0, MAX_LIST) : names;
                            StringBuilder body = new StringBuilder();
                            for (String n : show) {
                                body.append("  ").append(n).append("\n");
                            }
                            String header =
                                    Text.translatable(
                                                    "auraskills.granite.command.preset.list_header",
                                                    total)
                                            .getString();
                            String out = header + "\n" + body;
                            if (truncated) {
                                out +=
                                        Text.translatable(
                                                        "auraskills.granite.command.preset"
                                                                + ".list_truncated",
                                                        total - MAX_LIST,
                                                        MAX_LIST)
                                                .getString();
                            }
                            String finalOut = out;
                            source.sendFeedback(() -> Text.literal(finalOut), false);
                            return 1;
                        })
                .orElseGet(
                        () -> {
                            ctx.getSource()
                                    .sendError(Text.literal("AuraSkills is still starting."));
                            return 0;
                        });
    }

    private static List<String> discoverPresetZipNames(File pluginFolder) {
        File dir = new File(pluginFolder, "presets");
        if (!dir.isDirectory()) {
            return List.of();
        }
        File[] files = dir.listFiles((d, n) -> n.endsWith(".zip"));
        if (files == null || files.length == 0) {
            return List.of();
        }
        List<String> names = new ArrayList<>();
        for (File f : files) {
            if (FabricPresetNameValidator.validateZipFileName(f.getName()).isEmpty()) {
                names.add(f.getName());
            }
        }
        Collections.sort(names);
        return names;
    }

    private static int executeInfo(CommandContext<ServerCommandSource> ctx) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            ServerCommandSource source = ctx.getSource();
                            Locale lo = localeForSource(source, p);
                            String nameArg = StringArgumentType.getString(ctx, ARG_NAME);
                            ConfigPreset preset = loadPrepared(p, nameArg, source, lo);
                            if (preset == null) {
                                return 0;
                            }
                            String entries = getConfirmEntries(p, preset, lo);
                            String header =
                                    Text.translatable(
                                                    "auraskills.granite.command.preset.info_header",
                                                    nameArg)
                                            .getString();
                            String footer =
                                    Text.translatable(
                                                    "auraskills.granite.command.preset.info_footer")
                                            .getString();
                            String out = header + "\n" + entries + "\n" + footer;
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

    private static int executeLoadPreview(CommandContext<ServerCommandSource> ctx) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            ServerCommandSource source = ctx.getSource();
                            Locale lo = localeForSource(source, p);
                            String nameArg = StringArgumentType.getString(ctx, ARG_NAME);
                            ConfigPreset preset = loadPrepared(p, nameArg, source, lo);
                            if (preset == null) {
                                return 0;
                            }
                            String entries = getConfirmEntries(p, preset, lo);
                            String confirm =
                                    p.getMessageProvider()
                                            .applyFormatting(
                                                    TextUtil.replace(
                                                            p.getMsg(
                                                                    CommandMessage.PRESET_LOAD_CONFIRM,
                                                                    lo),
                                                            "{entries}",
                                                            entries));
                            String extra =
                                    Text.translatable(
                                                    "auraskills.granite.command.preset"
                                                            + ".load_preview_run_confirm",
                                                    nameArg)
                                            .getString();
                            String out = p.getPrefix(lo) + confirm + "\n" + extra;
                            source.sendFeedback(() -> Text.literal(out), false);
                            File zip = FabricPresetNameValidator.resolveSafePresetZip(
                                    p.getPluginFolder(), nameArg);
                            if (zip != null) {
                                FabricCommandConfirmation.registerPending(
                                        p, source, zip.getName());
                            }
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
                            String nameArg = StringArgumentType.getString(ctx, ARG_NAME);
                            File zip = FabricPresetNameValidator.resolveSafePresetZip(
                                    p.getPluginFolder(), nameArg);
                            if (zip == null) {
                                source.sendError(
                                        Text.translatable(
                                                "auraskills.granite.command.preset"
                                                        + ".path_not_in_presets"));
                                return 0;
                            }
                            if (!FabricCommandConfirmation.verifyPendingOrError(
                                    source, zip.getName())) {
                                return 0;
                            }
                            String loading =
                                    Text.translatable(
                                                    "auraskills.granite.command.preset.loading")
                                            .getString();
                            source.sendFeedback(() -> Text.literal(p.getPrefix(lo) + loading), true);
                            p.getScheduler()
                                    .executeAsync(() -> runLoadApply(p, source, lo, nameArg));
                            return 1;
                        })
                .orElseGet(
                        () -> {
                            ctx.getSource()
                                    .sendError(Text.literal("AuraSkills is still starting."));
                            return 0;
                        });
    }

    private static void runLoadApply(
            FabricAuraSkills p, ServerCommandSource source, Locale lo, String nameArg) {
        try {
            File pre = p.getBackupProvider().saveBackupSync(true);
            if (pre == null) {
                p.getScheduler()
                        .executeSync(
                                () ->
                                        source.sendError(
                                                Text.translatable(
                                                        "auraskills.granite.command.preset"
                                                                + ".preapply_skipped")));
                return;
            }
        } catch (Exception e) {
            p.logger().severe("Pre-apply UserState backup failed before preset load", e);
            p.getScheduler()
                    .executeSync(
                            () ->
                                    source.sendError(
                                            Text.translatable(
                                                    "auraskills.granite.command.preset"
                                                            + ".preapply_failed")));
            return;
        }

        ConfigPreset preset;
        try {
            preset = p.getPresetManager().preparePreset(nameArg);
        } catch (PresetLoadException e) {
            p.getScheduler()
                    .executeSync(
                            () ->
                                    source.sendError(
                                            Text.translatable(
                                                    "auraskills.granite.command.preset.error",
                                                    e.getMessage())));
            return;
        } catch (RuntimeException e) {
            p.logger().severe("Preset prepare failed", e);
            p.getScheduler()
                    .executeSync(
                            () ->
                                    source.sendError(
                                            Text.translatable(
                                                    "auraskills.granite.command.preset.error",
                                                    String.valueOf(e.getMessage()))));
            return;
        }

        try {
            PresetLoadResult result = p.getPresetManager().loadPreset(preset);
            p.getScheduler()
                    .executeSync(
                            () -> {
                                try {
                                    p.reloadFromDisk();
                                } catch (Exception e) {
                                    p.logger().severe("reloadFromDisk after preset failed", e);
                                    source.sendError(
                                            Text.translatable(
                                                    "auraskills.granite.command.preset.reload_failed",
                                                    String.valueOf(e.getMessage())));
                                    return;
                                }
                                String results = formatLoadResult(p, result, lo);
                                String loaded =
                                        p.getMessageProvider()
                                                .applyFormatting(
                                                        TextUtil.replace(
                                                                p.getMsg(
                                                                        CommandMessage
                                                                                .PRESET_LOAD_LOADED,
                                                                        lo),
                                                                "{name}",
                                                                nameArg));
                                String reloadNote =
                                        Text.translatable(
                                                        "auraskills.granite.command.preset.reload_note")
                                                .getString();
                                source.sendFeedback(
                                        () -> Text.literal(p.getPrefix(lo) + results + loaded),
                                        false);
                                source.sendFeedback(() -> Text.literal(reloadNote), false);
                            });
        } catch (Exception e) {
            p.logger().severe("Preset load failed", e);
            p.getScheduler()
                    .executeSync(
                            () ->
                                    source.sendError(
                                            Text.translatable(
                                                    "auraskills.granite.command.preset.error",
                                                    String.valueOf(e.getMessage()))));
        }
    }

    private static String formatLoadResult(FabricAuraSkills p, PresetLoadResult result, Locale lo) {
        var sb = new StringBuilder();
        appendResultBlock(
                p, sb, result.created(), CommandMessage.PRESET_LOAD_CREATED, lo);
        appendResultBlock(
                p, sb, result.modified(), CommandMessage.PRESET_LOAD_MODIFIED, lo);
        appendResultBlock(
                p, sb, result.replaced(), CommandMessage.PRESET_LOAD_REPLACED, lo);
        appendResultBlock(
                p, sb, result.deleted(), CommandMessage.PRESET_LOAD_DELETED, lo);
        appendResultBlock(
                p, sb, result.skipped(), CommandMessage.PRESET_LOAD_SKIPPED, lo);
        return sb.toString();
    }

    private static void appendResultBlock(
            FabricAuraSkills p,
            StringBuilder sb,
            List<String> files,
            CommandMessage headerKey,
            Locale lo) {
        if (files.isEmpty()) {
            return;
        }
        sb.append(p.getMessageProvider().applyFormatting(p.getMsg(headerKey, lo)));
        sb.append(":\n");
        for (String f : files) {
            sb.append("  ").append(f).append("\n");
        }
    }

    private static @Nullable ConfigPreset loadPrepared(
            FabricAuraSkills p, String nameArg, ServerCommandSource source, Locale lo) {
        if (FabricPresetNameValidator.validateZipFileName(nameArg).isPresent()) {
            source.sendError(Text.translatable("auraskills.granite.command.preset.unsafe_name"));
            return null;
        }
        File zip = FabricPresetNameValidator.resolveSafePresetZip(p.getPluginFolder(), nameArg);
        if (zip == null) {
            source.sendError(
                    Text.translatable("auraskills.granite.command.preset.path_not_in_presets"));
            return null;
        }
        if (!zip.isFile()) {
            source.sendError(Text.translatable("auraskills.granite.command.preset.missing_zip"));
            return null;
        }
        try {
            return p.getPresetManager().preparePreset(nameArg);
        } catch (PresetLoadException e) {
            source.sendError(
                    Text.translatable("auraskills.granite.command.preset.error", e.getMessage()));
            return null;
        }
    }

    private static String getConfirmEntries(FabricAuraSkills p, ConfigPreset preset, Locale lo) {
        var sb = new StringBuilder();
        sb.append("\n");
        for (PresetEntry entry : preset.entries()) {
            String basePath = "commands.preset.load.";
            MessageKey key =
                    switch (entry.action()) {
                        case APPEND -> MessageKey.of(basePath + "appended_entry");
                        case MERGE -> MessageKey.of(basePath + "merged_entry");
                        case REPLACE -> MessageKey.of(basePath + "replaced_entry");
                        case DELETE -> MessageKey.of(basePath + "deleted_entry");
                    };
            sb.append("  ");
            sb.append(
                    p.getMessageProvider()
                            .applyFormatting(
                                    TextUtil.replace(
                                            p.getMsg(key, lo), "{file}", entry.name())));
            sb.append("\n");
        }
        return sb.toString();
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
