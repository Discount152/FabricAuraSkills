package dev.aurelium.auraskills.fabric;

import net.minecraft.entity.projectile.FishingBobberEntity;
import org.jetbrains.annotations.Nullable;

/**
 * While {@link net.minecraft.entity.projectile.FishingBobberEntity#use} runs on the server thread, exposes the active
 * bobber so {@link net.minecraft.entity.player.PlayerEntity#increaseStat} can attribute {@code fish_caught} to XP.
 */
public final class FabricFishingBobberUseContext {

    private static final ThreadLocal<FishingBobberEntity> ACTIVE = new ThreadLocal<>();

    private FabricFishingBobberUseContext() {}

    public static void enter(FishingBobberEntity bobber) {
        ACTIVE.set(bobber);
    }

    public static void leave() {
        ACTIVE.remove();
    }

    public static @Nullable FishingBobberEntity peek() {
        return ACTIVE.get();
    }
}
