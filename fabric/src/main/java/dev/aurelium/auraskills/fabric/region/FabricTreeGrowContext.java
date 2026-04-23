package dev.aurelium.auraskills.fabric.region;

import org.jetbrains.annotations.Nullable;

/**
 * Carries the {@code y} of the current {@link net.minecraft.world.gen.feature.TreeFeature} generation
 * (Bukkit {@code StructureGrowEvent} / sapling grow height) for {@code check_replace} cleanup.
 * Tree generation is single-threaded on the server; use a thread local for the duration of
 * {@code Feature#generate} on {@code TreeFeature}.
 */
public final class FabricTreeGrowContext {

    private static final ThreadLocal<@Nullable Integer> SAPLING_GROUND_Y = new ThreadLocal<>();

    private FabricTreeGrowContext() {
    }

    public static void begin(int growY) {
        SAPLING_GROUND_Y.set(growY);
    }

    public static void end() {
        SAPLING_GROUND_Y.remove();
    }

    public static @Nullable Integer currentGrowY() {
        return SAPLING_GROUND_Y.get();
    }
}
