package dev.granite.graniteskills.fabric;

import dev.granite.graniteskills.fabric.command.FabricBrigadierCommands;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

/**
 * Registers the restored Granite command tree. Vanilla {@code /xp} and {@code /item} stay vanilla;
 * Granite item and XP operations live under {@code /graniteskills}, {@code /skills}, {@code /sk}, and {@code /skill}.
 */
public final class FabricGraniteSkillsCommands {

    private FabricGraniteSkillsCommands() {
    }

    public static void registerEarly() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                FabricBrigadierCommands.register(dispatcher));
    }
}
