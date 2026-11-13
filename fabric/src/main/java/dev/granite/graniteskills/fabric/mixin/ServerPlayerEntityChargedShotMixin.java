package dev.granite.graniteskills.fabric.mixin;

import dev.granite.graniteskills.fabric.FabricChargedShot;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.SwingAnimation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerEntityChargedShotMixin {

    @Inject(
            method = "swingAndResetAttackStrength(Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/component/SwingAnimation;Z)V",
            at = @At("HEAD")
    )
    private void graniteskills$chargedShotToggleOnSwing(InteractionHand hand, SwingAnimation animation, boolean reset, CallbackInfo ci) {
        if (hand != InteractionHand.MAIN_HAND) {
            return;
        }
        ServerPlayer self = (ServerPlayer) (Object) this;
        if (!self.getMainHandItem().is(Items.BOW)) {
            return;
        }
        FabricGraniteSkills plugin = FabricGraniteSkills.getInstance();
        if (plugin != null) {
            FabricChargedShot.onSwingMainHandBow(plugin, self);
        }
    }
}
