package dev.granite.graniteskills.fabric.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.granite.graniteskills.fabric.FabricXpDiagnostics;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import static net.minecraft.commands.Commands.literal;

public final class FabricXpDiagnosticsCommands {

    private FabricXpDiagnosticsCommands() {}

    /** Operator level required when no permission provider grants {@link FabricCommandPermissions#XP_DIAG}. */
    private static final int XP_DIAG_OP_LEVEL = 2;

    public static LiteralArgumentBuilder<CommandSourceStack> xpDiagRoot() {
        return literal("xpdiag")
                .requires(FabricCommandRequirements.permissionOrOpLevel(
                        FabricCommandPermissions.XP_DIAG, XP_DIAG_OP_LEVEL))
                .executes(FabricXpDiagnosticsCommands::status)
                .then(literal("on").executes(ctx -> setEnabled(ctx, true)))
                .then(literal("off").executes(ctx -> setEnabled(ctx, false)))
                .then(literal("status").executes(FabricXpDiagnosticsCommands::status));
    }

    private static int setEnabled(CommandContext<CommandSourceStack> ctx, boolean enabled) {
        return FabricCommandPluginState.runtimeReadyOrEmpty()
                .map(p -> {
                    FabricXpDiagnostics.setEnabled(enabled);
                    p.logger().info("XP diagnostics " + (enabled ? "enabled" : "disabled") + " by command source.");
                    ctx.getSource().sendSuccess(
                            () -> Component.literal("GraniteSkills XP diagnostics: " + (enabled ? "ON" : "OFF")),
                            true);
                    return 1;
                })
                .orElseGet(() -> {
                    ctx.getSource().sendFailure(Component.literal("GraniteSkills is still starting."));
                    return 0;
                });
    }

    private static int status(CommandContext<CommandSourceStack> ctx) {
        return FabricCommandPluginState.pluginOrEmpty()
                .map(p -> {
                    ctx.getSource().sendSuccess(
                            () -> Component.literal(
                                    "GraniteSkills XP diagnostics: " + (FabricXpDiagnostics.isEnabled() ? "ON" : "OFF")),
                            false);
                    return 1;
                })
                .orElseGet(() -> {
                    ctx.getSource().sendFailure(Component.literal("GraniteSkills is still starting."));
                    return 0;
                });
    }
}
