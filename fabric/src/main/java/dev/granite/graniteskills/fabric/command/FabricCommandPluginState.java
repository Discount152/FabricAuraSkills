package dev.granite.graniteskills.fabric.command;

import dev.granite.graniteskills.fabric.FabricGraniteSkills;

import java.util.Optional;

/**
 * Small helpers for “mod still starting” / runtime readiness — keeps executors
 * consistent across command batches.
 */
public final class FabricCommandPluginState {

    private FabricCommandPluginState() {}

    public static Optional<FabricGraniteSkills> pluginOrEmpty() {
        return Optional.ofNullable(FabricGraniteSkills.getInstance());
    }

    public static Optional<FabricGraniteSkills> runtimeReadyOrEmpty() {
        return pluginOrEmpty().filter(FabricGraniteSkills::isRuntimeReady);
    }
}
