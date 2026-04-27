package dev.aurelium.auraskills.fabric.command;

import com.mojang.brigadier.context.CommandContext;
import dev.aurelium.auraskills.fabric.menu.FabricGraniteBundledMenuTemplateIds;
import dev.aurelium.auraskills.fabric.menu.FabricGraniteMenuTemplateExporter;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.literal;

/**
 * Sprint 108: staff export of bundled Granite menu templates to {@code config/auraskills/granite/templates/}.
 */
public final class FabricGraniteTemplateCommands {

    private FabricGraniteTemplateCommands() {}

    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<ServerCommandSource> templatesLiteral() {
        return literal("templates")
                .then(literal("export")
                        .requires(
                                FabricCommandRequirements.permissionOrNonPlayerSource(
                                        FabricCommandPermissions.TEMPLATES, false))
                        .executes(FabricGraniteTemplateCommands::templatesExport));
    }

    private static int templatesExport(CommandContext<ServerCommandSource> ctx) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(
                        p -> {
                            FabricGraniteMenuTemplateExporter.ExportOutcome out =
                                    FabricGraniteMenuTemplateExporter.exportMissing(
                                            p.getPluginFolder().toPath(),
                                            cp ->
                                                    FabricGraniteBundledMenuTemplateIds.class
                                                            .getClassLoader()
                                                            .getResourceAsStream(cp));
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
}
