package dev.granite.graniteskills.fabric.mixin;

import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import dev.granite.graniteskills.fabric.ability.FabricFightingAbilities;
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
    private void graniteskills$parryReadyOnSwing(InteractionHand hand, SwingAnimation animation, boolean reset, CallbackInfo ci) {
        FabricGraniteSkills plugin = FabricGraniteSkills.getInstance();
        if (plugin == null) {
            return;
        }
        FabricFightingAbilities.onMainHandSwing(plugin, (ServerPlayer) (Object) this, hand);
    }
}
