package dev.aurelium.auraskills.fabric.mixin;

import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.ability.FabricFightingAbilities;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.component.SwingAnimation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mirrors Bukkit {@code PlayerInteractEvent} left-click readiness for Parry (sword swing in main hand).
 */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerEntityParrySwingMixin {

    @Inject(
            method = "swingAndResetAttackStrength(Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/component/SwingAnimation;Z)V",
            at = @At("HEAD")
    )
    private void auraskills$parryReadyOnSwing(InteractionHand hand, SwingAnimation animation, boolean reset, CallbackInfo ci) {
        FabricAuraSkills plugin = FabricAuraSkills.getInstance();
        if (plugin == null) {
            return;
        }
        FabricFightingAbilities.onMainHandSwing(plugin, (ServerPlayer) (Object) this, hand);
    }
}
