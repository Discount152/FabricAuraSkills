package dev.granite.graniteskills.api;

import org.jetbrains.annotations.ApiStatus.Internal;

public final class GraniteSkillsProvider {

    private static GraniteSkillsApi instance = null;

    /**
     * Gets the instance of {@link GraniteSkillsApi} containing API classes and methods.
     *
     * @return the API instance
     */
    public static GraniteSkillsApi getInstance() {
        GraniteSkillsApi instance = GraniteSkillsProvider.instance;
        if (instance == null) {
            throw new IllegalStateException("GraniteSkillsApi is not initialized");
        }
        return instance;
    }

    @Internal
    static void register(GraniteSkillsApi instance) {
        GraniteSkillsProvider.instance = instance;
    }

    @Internal
    static void unregister() {
        GraniteSkillsProvider.instance = null;
    }

    @Internal
    private GraniteSkillsProvider() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

}
