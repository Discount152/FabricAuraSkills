package dev.granite.graniteskills.api;

import org.jetbrains.annotations.ApiStatus;

public final class GraniteSkillsBukkitProvider {

    private static GraniteSkillsBukkit instance = null;

    /**
     * Gets the instance of {@link GraniteSkillsApi} containing API classes and methods.
     *
     * @return the API instance
     */
    public static GraniteSkillsBukkit getInstance() {
        GraniteSkillsBukkit instance = GraniteSkillsBukkitProvider.instance;
        if (instance == null) {
            throw new IllegalStateException("GraniteSkillsBukkit is not initialized");
        }
        return instance;
    }

    @ApiStatus.Internal
    static void register(GraniteSkillsBukkit instance) {
        GraniteSkillsBukkitProvider.instance = instance;
    }

    @ApiStatus.Internal
    static void unregister() {
        GraniteSkillsBukkitProvider.instance = null;
    }

    @ApiStatus.Internal
    private GraniteSkillsBukkitProvider() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

}
