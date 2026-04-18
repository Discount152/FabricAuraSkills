package dev.aurelium.auraskills.fabric.mixin;

import dev.aurelium.auraskills.fabric.FabricAbsorption;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Bukkit Absorption readies on {@code LEFT_CLICK_AIR} as well as {@code LEFT_CLICK_BLOCK}; Fabric previously only used
 * {@link net.fabricmc.fabric.api.event.player.AttackBlockCallback}. This mirrors the air swing path for main-hand shield.
 */
@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityShieldAbsorptionSwingMixin {

    @Inject(method = "swingHand(Lnet/minecraft/util/Hand;)V", at = @At("HEAD"))
    private void auraskills$absorptionReadyOnSwing(Hand hand, CallbackInfo ci) {
        FabricAuraSkills plugin = FabricAuraSkills.getInstance();
        if (plugin == null) {
            return;
        }
        FabricAbsorption.onMainHandShieldSwing(plugin, (ServerPlayerEntity) (Object) this, hand);
    }
}
