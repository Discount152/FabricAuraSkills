package dev.aurelium.auraskills.fabric.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.aurelium.auraskills.fabric.FabricXpDiagnostics;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.literal;

public final class FabricXpDiagnosticsCommands {

    private FabricXpDiagnosticsCommands() {}

    /** Operator level required when no permission provider grants {@link FabricCommandPermissions#XP_DIAG}. */
    private static final int XP_DIAG_OP_LEVEL = 2;

    public static LiteralArgumentBuilder<ServerCommandSource> xpDiagRoot() {
        return literal("xpdiag")
                .requires(FabricCommandRequirements.permissionOrOpLevel(
                        FabricCommandPermissions.XP_DIAG, XP_DIAG_OP_LEVEL))
                .executes(FabricXpDiagnosticsCommands::status)
                .then(literal("on").executes(ctx -> setEnabled(ctx, true)))
                .then(literal("off").executes(ctx -> setEnabled(ctx, false)))
                .then(literal("status").executes(FabricXpDiagnosticsCommands::status));
    }

    private static int setEnabled(CommandContext<ServerCommandSource> ctx, boolean enabled) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(p -> {
                    FabricXpDiagnostics.setEnabled(enabled);
                    p.logger().info("XP diagnostics " + (enabled ? "enabled" : "disabled") + " by command source.");
                    ctx.getSource().sendFeedback(
                            () -> Text.literal("AuraSkills XP diagnostics: " + (enabled ? "ON" : "OFF")),
                            true);
                    return 1;
                })
                .orElseGet(() -> {
                    ctx.getSource().sendError(Text.literal("AuraSkills is still starting."));
                    return 0;
                });
    }

    private static int status(CommandContext<ServerCommandSource> ctx) {
        return FabricCommandPluginState.pluginOrEmpty()
                .map(p -> {
                    ctx.getSource().sendFeedback(
                            () -> Text.literal(
                                    "AuraSkills XP diagnostics: " + (FabricXpDiagnostics.isEnabled() ? "ON" : "OFF")),
                            false);
                    return 1;
                })
                .orElseGet(() -> {
                    ctx.getSource().sendError(Text.literal("AuraSkills is still starting."));
                    return 0;
                });
    }
}
