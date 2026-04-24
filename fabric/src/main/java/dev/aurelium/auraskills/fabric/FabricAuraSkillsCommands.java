package dev.aurelium.auraskills.fabric;

import com.mojang.brigadier.CommandDispatcher;
import dev.aurelium.auraskills.fabric.command.FabricBrigadierCommands;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;

/**
 * Fabric entry for command registration. **Sprint 39+:** implementation lives in
 * {@link dev.aurelium.auraskills.fabric.command.FabricBrigadierCommands} (shared
 * permission nodes, grouped registration, extension point for future batches).
 */
public final class FabricAuraSkillsCommands {

    private FabricAuraSkillsCommands() {
    }

    public static void registerEarly() {
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) -> register(dispatcher));
    }

    private static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        FabricBrigadierCommands.register(dispatcher);
    }
}
