package dev.aurelium.auraskills.fabric.mixin;

import dev.aurelium.auraskills.fabric.ability.FabricEnduranceAbilities;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerFoodExhaustionMixin {

    @Inject(method = "causeFoodExhaustion", at = @At("HEAD"), cancellable = true)
    private void auraskills$antiHunger(float exhaustion, CallbackInfo ci) {
        if (exhaustion <= 0f || !((Object) this instanceof ServerPlayer player)) {
            return;
        }
        if (FabricEnduranceAbilities.shouldIgnoreHunger(player)) {
            ci.cancel();
        }
    }
}
