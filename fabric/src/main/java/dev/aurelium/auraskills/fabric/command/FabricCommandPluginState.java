package dev.aurelium.auraskills.fabric.command;

import dev.aurelium.auraskills.fabric.FabricAuraSkills;

import java.util.Optional;

/**
 * Small helpers for “mod still starting” / runtime readiness — keeps executors
 * consistent across command batches.
 */
public final class FabricCommandPluginState {

    private FabricCommandPluginState() {}

    public static Optional<FabricAuraSkills> pluginOrEmpty() {
        return Optional.ofNullable(FabricAuraSkills.getInstance());
    }

    public static Optional<FabricAuraSkills> runtimeReadyOrEmpty() {
        return pluginOrEmpty().filter(FabricAuraSkills::isRuntimeReady);
    }
}
