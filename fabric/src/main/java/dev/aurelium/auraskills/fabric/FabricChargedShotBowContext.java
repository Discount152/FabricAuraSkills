package dev.aurelium.auraskills.fabric;

import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Holds bow draw strength between {@link net.minecraft.item.BowItem#onStoppedUsing} and
 * {@link net.minecraft.item.BowItem#shoot} on the server thread (same as Bukkit
 * {@code EntityShootBowEvent} timing).
 */
final class FabricChargedShotBowContext {

    private static final ThreadLocal<Context> TL = new ThreadLocal<>();

    private FabricChargedShotBowContext() {
    }

    static void set(ServerPlayerEntity player, float pullProgress) {
        TL.set(new Context(player, pullProgress));
    }

    /**
     * If the current thread-local context matches {@code shooter}, removes and returns it; otherwise returns null.
     */
    static @Nullable Context takeIfShooterMatches(net.minecraft.entity.LivingEntity shooter) {
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

    record Context(ServerPlayerEntity player, float pullProgress) {
    }
}
