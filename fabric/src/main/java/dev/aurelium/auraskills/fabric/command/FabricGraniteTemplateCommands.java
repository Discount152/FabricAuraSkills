package dev.aurelium.auraskills.fabric.command;

import com.mojang.brigadier.context.CommandContext;
import dev.aurelium.auraskills.fabric.menu.FabricGraniteBundledMenuTemplateIds;
import dev.aurelium.auraskills.fabric.menu.FabricGraniteMenuTemplateExporter;
import dev.aurelium.auraskills.fabric.text.FabricGraniteText;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.literal;

/**
 * Sprint 108: staff export of bundled Granite menu templates to {@code config/auraskills/granite/templates/}.
 *
 * <p>Sprint 109: guarded overwrite — preview ({@code templates export overwrite}) and console/RCON-only confirm
 * ({@code … overwrite confirm}) via {@link FabricCommandConfirmation}.
 */
public final class FabricGraniteTemplateCommands {

    private FabricGraniteTemplateCommands() {}

    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<ServerCommandSource> templatesLiteral() {
        return literal("templates")
                .then(
                        literal("export")
                                .requires(
                                        FabricCommandRequirements.permissionOrNonPlayerSource(
                                                FabricCommandPermissions.TEMPLATES, false))
                                .executes(FabricGraniteTemplateCommands::templatesExport)
                                .then(
                                        literal("overwrite")
                                                .requires(
                                                        FabricCommandRequirements.permissionOrNonPlayerSource(
                                                                FabricCommandPermissions.TEMPLATES, false))
                                                .executes(FabricGraniteTemplateCommands::templatesExportOverwritePreview)
                                                .then(
                                                        literal("confirm")
                                                                .requires(
                                                                        FabricCommandRequirements
                                                                                .permissionOrNonPlayerSource(
                                                                                        FabricCommandPermissions
                                                                                                .TEMPLATES,
                                                                                        false))
                                                                .executes(
                                                                        FabricGraniteTemplateCommands
                                                                                ::templatesExportOverwriteConfirm))));
    }

    private static FabricGraniteMenuTemplateExporter.BundledOpener bundledOpener() {
        return cp ->
                FabricGraniteBundledMenuTemplateIds.class.getClassLoader().getResourceAsStream(cp);
    }

    private static int templatesExport(CommandContext<ServerCommandSource> ctx) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            FabricGraniteMenuTemplateExporter.ExportOutcome out =
                                    FabricGraniteMenuTemplateExporter.exportMissing(
                                            p.getPluginFolder().toPath(), bundledOpener());
                            ServerCommandSource source = ctx.getSource();
                            String abs = out.templatesDirectory().toAbsolutePath().normalize().toString();
                            StringBuilder summary =
                                    new StringBuilder(
                                            "Exported Granite menu templates: created "
                                                    + out.created()
                                                    + ", skipped "
                                                    + out.skipped()
                                                    + ", failed "
                                                    + out.failed()
                                                    + ".\nPath: "
                                                    + abs);
                            if (out.skipped() > 0) {
                                summary.append("\nExisting files were not overwritten.");
                            }
                            if (out.failed() > 0) {
                                summary.append("\nFailures:");
                                for (FabricGraniteMenuTemplateExporter.FileOutcome f : out.files()) {
                                    if (f.kind() == FabricGraniteMenuTemplateExporter.FileOutcome.Kind.FAILED) {
                                        summary.append("\n  - ")
                                                .append(f.menuId())
                                                .append(": ")
                                                .append(f.detail() != null ? f.detail() : "(unknown)");
                                    }
                                }
                            }
                            String summaryText = summary.toString();
                            source.sendFeedback(() -> Text.literal(summaryText), false);
                            source.sendFeedback(
                                    () ->
                                            Text.literal(
                                                    "Templates are read on each menu open; run /auraskills reload to reset template diagnostic debounce after edits."),
                                    false);
                            return 1;
                        })
                .orElseGet(
                        () -> {
                            ctx.getSource().sendError(Text.literal("AuraSkills is still starting."));
                            return 0;
                        });
    }

    private static int templatesExportOverwritePreview(CommandContext<ServerCommandSource> ctx) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            FabricGraniteMenuTemplateExporter.OverwritePreviewOutcome pre =
                                    FabricGraniteMenuTemplateExporter.previewOverwrite(
                                            p.getPluginFolder().toPath(), bundledOpener());
                            ServerCommandSource source = ctx.getSource();
                            String abs =
                                    pre.templatesDirectory().toAbsolutePath().normalize().toString();
                            String previewBody =
                                    "Granite template overwrite preview:\nWould create: "
                                            + pre.wouldCreate()
                                            + "\nWould overwrite: "
                                            + pre.wouldOverwrite()
                                            + "\nWould fail: "
                                            + pre.wouldFail()
                                            + "\nPath: "
                                            + abs;
                            source.sendFeedback(() -> Text.literal(previewBody), false);
                            if (pre.wouldFail() > 0) {
                                StringBuilder failLines = new StringBuilder("Forecast failures:");
                                for (FabricGraniteMenuTemplateExporter.PreviewFileOutcome f : pre.files()) {
                                    if (f.kind()
                                            == FabricGraniteMenuTemplateExporter.PreviewFileOutcome.PreviewKind
                                                    .WOULD_FAIL) {
                                        failLines.append("\n  - ")
                                                .append(f.menuId())
                                                .append(": ")
                                                .append(f.detail() != null ? f.detail() : "(unknown)");
                                    }
                                }
                                String fl = failLines.toString();
                                source.sendFeedback(() -> Text.literal(fl), false);
                            }

                            boolean consoleOrRcon = !(source.getEntity() instanceof ServerPlayerEntity);
                            if (consoleOrRcon) {
                                FabricCommandConfirmation.registerPending(
                                        p, source, FabricCommandConfirmation.TEMPLATES_EXPORT_OVERWRITE_PENDING);
                                source.sendFeedback(
                                        () ->
                                                Text.literal(
                                                        "Run /auraskills templates export overwrite confirm from this console or RCON within 30s to apply."),
                                        false);
                            } else {
                                source.sendFeedback(
                                        () ->
                                                FabricGraniteText.key(
                                                        "auraskills.granite.command.templates.overwrite"
                                                                + ".preview_arm_from_console"),
                                        false);
                            }

                            source.sendFeedback(
                                    () ->
                                            Text.literal(
                                                    "Already-open menus stay unchanged until reopened. Templates are read on each menu open; /auraskills reload resets template diagnostic debounce."),
                                    false);
                            return 1;
                        })
                .orElseGet(
                        () -> {
                            ctx.getSource().sendError(Text.literal("AuraSkills is still starting."));
                            return 0;
                        });
    }

    private static int templatesExportOverwriteConfirm(CommandContext<ServerCommandSource> ctx) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            ServerCommandSource source = ctx.getSource();
                            if (source.getEntity() instanceof ServerPlayerEntity) {
                                source.sendError(
                                        FabricGraniteText.key(
                                                "auraskills.granite.command.templates.overwrite"
                                                        + ".confirm_console_only"));
                                return 0;
                            }
                            if (!FabricCommandConfirmation.verifyPendingOrError(
                                    source, FabricCommandConfirmation.TEMPLATES_EXPORT_OVERWRITE_PENDING)) {
                                return 0;
                            }

                            FabricGraniteMenuTemplateExporter.OverwriteExportOutcome out =
                                    FabricGraniteMenuTemplateExporter.exportOverwrite(
                                            p.getPluginFolder().toPath(), bundledOpener());
                            String abs = out.templatesDirectory().toAbsolutePath().normalize().toString();
                            StringBuilder summary =
                                    new StringBuilder(
                                            "Exported Granite menu templates with overwrite: created "
                                                    + out.created()
                                                    + ", overwritten "
                                                    + out.overwritten()
                                                    + ", failed "
                                                    + out.failed()
                                                    + ".\nPath: "
                                                    + abs);
                            if (out.failed() > 0) {
                                summary.append("\nFailures:");
                                for (FabricGraniteMenuTemplateExporter.OverwriteFileOutcome f : out.files()) {
                                    if (f.kind()
                                            == FabricGraniteMenuTemplateExporter.OverwriteFileOutcome.OverwriteKind
                                                    .FAILED) {
                                        summary.append("\n  - ")
                                                .append(f.menuId())
                                                .append(": ")
                                                .append(f.detail() != null ? f.detail() : "(unknown)");
                                    }
                                }
                            }
                            String st = summary.toString();
                            source.sendFeedback(() -> Text.literal(st), false);
                            source.sendFeedback(
                                    () ->
                                            Text.literal(
                                                    "Templates are read on each menu open; /auraskills reload resets template diagnostic debounce. Already-open menus stay unchanged until reopened."),
                                    false);
                            return 1;
                        })
                .orElseGet(
                        () -> {
                            ctx.getSource().sendError(Text.literal("AuraSkills is still starting."));
                            return 0;
                        });
    }
}
