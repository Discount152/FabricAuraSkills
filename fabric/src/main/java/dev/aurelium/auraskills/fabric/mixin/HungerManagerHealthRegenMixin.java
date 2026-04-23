package dev.aurelium.auraskills.fabric.mixin;

import dev.aurelium.auraskills.fabric.FabricHealthRegen;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Bukkit {@code HealthRegenTraits#onRegen} parity for the two vanilla natural-regen
 * {@link ServerPlayerEntity#heal(float)} calls in {@link HungerManager#update(ServerPlayerEntity)}.
 */
@Mixin(HungerManager.class)
public abstract class HungerManagerHealthRegenMixin {

    @Redirect(
            method = "update",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayerEntity;heal(F)V",
                    ordinal = 0
            )
    )
    private void auraskills$redirectSatiatedHealFirst(ServerPlayerEntity player, float amount) {
        FabricHealthRegen.redirectFirstHealInUpdate(player, amount);
    }

    @Redirect(
            method = "update",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayerEntity;heal(F)V",
                    ordinal = 1
            )
    )
    private void auraskills$redirectSatiatedHealSecond(ServerPlayerEntity player, float amount) {
        FabricHealthRegen.redirectSecondHealInUpdate(player, amount);
    }
}
