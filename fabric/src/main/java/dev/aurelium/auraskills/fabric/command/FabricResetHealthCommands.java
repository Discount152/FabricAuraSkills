package dev.aurelium.auraskills.fabric.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.aurelium.auraskills.fabric.storage.FabricOfflinePlayerDataAuraStripping;
import dev.aurelium.auraskills.fabric.storage.FabricServerPlayerDataPaths;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static net.minecraft.server.command.CommandManager.literal;

/**
 * Bukkit {@code SkillsRootCommand#onResetHealth} — <strong>console/RCON only</strong>;
 * {@link FabricOfflinePlayerDataAuraStripping} mirrors 1.21+ {@code Uninstaller} for offline
 * playerdata. **Sprint 57.**
 */
public final class FabricResetHealthCommands {

    private FabricResetHealthCommands() {}

    public static LiteralArgumentBuilder<ServerCommandSource> resetHealthLiteral() {
        return literal("resethealth")
                .requires(
                        FabricCommandRequirements.permission(
                                FabricCommandPermissions.RESETHEALTH, false))
                .executes(FabricResetHealthCommands::execute);
    }

    private static int execute(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();
        if (source.isExecutedByPlayer() || source.getEntity() != null) {
            source.sendError(
                    Text.translatable("auraskills.granite.command.resethealth.console_only"));
            return 0;
        }
        MinecraftServer server = source.getServer();
        Set<UUID> online =
                server.getPlayerManager()
                        .getPlayerList()
                        .stream()
                        .map(p -> p.getUuid())
                        .collect(Collectors.toCollection(HashSet::new));
        try {
            FabricOfflinePlayerDataAuraStripping.Result r =
                    FabricOfflinePlayerDataAuraStripping.stripFromOfflinePlayerDataFiles(
                            FabricServerPlayerDataPaths.overworldPlayerData(server), online);
            source.sendFeedback(
                    () -> Text.translatable(
                            "auraskills.granite.command.resethealth.summary",
                            r.offlineFilesScanned(),
                            r.filesWithModifiersStripped(),
                            r.readWriteErrors()),
                    true);
        } catch (IOException e) {
            source.sendError(
                    Text.translatable(
                            "auraskills.granite.command.resethealth.io_error", e.getMessage()));
            return 0;
        }
        return 1;
    }
}
