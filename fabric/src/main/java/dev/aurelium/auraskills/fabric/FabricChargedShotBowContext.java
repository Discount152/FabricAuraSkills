package dev.aurelium.auraskills.fabric;

import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

/**
 * Holds bow draw strength between {@link net.minecraft.world.item.BowItem#releaseUsing} and
 * {@link net.minecraft.world.item.BowItem#shootProjectile} on the server thread (same as Bukkit
 * {@code EntityShootBowEvent} timing).
 */
final class FabricChargedShotBowContext {

    private static final ThreadLocal<Context> TL = new ThreadLocal<>();

    private FabricChargedShotBowContext() {
    }

    static void set(ServerPlayer player, float pullProgress) {
        TL.set(new Context(player, pullProgress));
    }

    /**
     * If the current thread-local context matches {@code shooter}, removes and returns it; otherwise returns null.
     */
    static @Nullable Context takeIfShooterMatches(net.minecraft.world.entity.LivingEntity shooter) {
        Context c = TL.get();
        if (c == null || c.player != shooter) {
            return null;
        }
        TL.remove();
        return c;
    }

    static void clear() {
        TL.remove();
    }

    record Context(ServerPlayer player, float pullProgress) {
    }
}
