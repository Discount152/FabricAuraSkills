package dev.aurelium.auraskills.fabric.mixin;

import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.ability.FabricFightingAbilities;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mirrors Bukkit {@code PlayerInteractEvent} left-click readiness for Parry (sword swing in main hand).
 */
@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityParrySwingMixin {

    @Inject(method = "swingHand(Lnet/minecraft/util/Hand;)V", at = @At("HEAD"))
    private void auraskills$parryReadyOnSwing(Hand hand, CallbackInfo ci) {
        FabricAuraSkills plugin = FabricAuraSkills.getInstance();
        if (plugin == null) {
            return;
        }
        FabricFightingAbilities.onMainHandSwing(plugin, (ServerPlayerEntity) (Object) this, hand);
    }
}
