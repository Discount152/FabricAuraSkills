package dev.granite.graniteskills.fabric.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.granite.graniteskills.fabric.storage.FabricOfflinePlayerDataAuraStripping;
import dev.granite.graniteskills.fabric.storage.FabricServerPlayerDataPaths;
import dev.granite.graniteskills.fabric.text.FabricGraniteText;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static net.minecraft.commands.Commands.literal;

/**
 * Bukkit {@code SkillsRootCommand#onResetHealth} — <strong>console/RCON only</strong>;
 * {@link FabricOfflinePlayerDataAuraStripping} mirrors 1.21+ {@code Uninstaller} for offline
 * playerdata. **Sprint 57.**
 */
public final class FabricResetHealthCommands {

    private FabricResetHealthCommands() {}

    public static LiteralArgumentBuilder<CommandSourceStack> resetHealthLiteral() {
        return literal("resethealth")
                .requires(
                        FabricCommandRequirements.permission(
                                FabricCommandPermissions.RESETHEALTH, false))
                .executes(FabricResetHealthCommands::execute);
    }

    private static int execute(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        if (source.isPlayer() || source.getEntity() != null) {
            source.sendFailure(
                    FabricGraniteText.key("graniteskills.granite.command.resethealth.console_only"));
            return 0;
        }
        MinecraftServer server = source.getServer();
        Set<UUID> online =
                server.getPlayerList().getPlayers()
                        .stream()
                        .map(p -> p.getUUID())
                        .collect(Collectors.toCollection(HashSet::new));
        try {
            FabricOfflinePlayerDataAuraStripping.Result r =
                    FabricOfflinePlayerDataAuraStripping.stripFromOfflinePlayerDataFiles(
                            FabricServerPlayerDataPaths.overworldPlayerData(server), online);
            source.sendSuccess(
                    () -> FabricGraniteText.key(
                            "graniteskills.granite.command.resethealth.summary",
                            r.offlineFilesScanned(),
                            r.filesWithModifiersStripped(),
                            r.readWriteErrors()),
                    true);
        } catch (IOException e) {
            source.sendFailure(
                    FabricGraniteText.key(
                            "graniteskills.granite.command.resethealth.io_error", e.getMessage()));
            return 0;
        }
        return 1;
    }
}
