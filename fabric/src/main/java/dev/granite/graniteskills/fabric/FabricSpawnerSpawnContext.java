package dev.granite.graniteskills.fabric;

/**
 * Marks the server thread while {@link net.minecraft.world.level.BaseSpawner#delay}
 * is running so {@link net.minecraft.server.level.ServerLevel#addFreshEntity} can tag spawner mobs.
 */
public final class FabricSpawnerSpawnContext {

    private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);

    private FabricSpawnerSpawnContext() {
    }

    public static void enter() {
        DEPTH.set(DEPTH.get() + 1);
    }

    public static void exit() {
        int d = DEPTH.get() - 1;
        if (d <= 0) {
            DEPTH.remove();
        } else {
            DEPTH.set(d);
        }
    }

    public static boolean active() {
        return DEPTH.get() > 0;
    }
}
