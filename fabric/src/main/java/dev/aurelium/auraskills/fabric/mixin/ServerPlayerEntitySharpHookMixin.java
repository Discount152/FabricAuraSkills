package dev.aurelium.auraskills.fabric.mixin;

import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.FabricSharpHook;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.SwingAnimation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerEntitySharpHookMixin {

    @Inject(
            method = "swingAndResetAttackStrength(Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/component/SwingAnimation;Z)V",
            at = @At("HEAD")
    )
    private void auraskills$sharpHookOnSwing(InteractionHand hand, SwingAnimation animation, boolean reset, CallbackInfo ci) {
        if (hand != InteractionHand.MAIN_HAND) {
            return;
        }
        ServerPlayer self = (ServerPlayer) (Object) this;
        if (!self.getMainHandItem().is(Items.FISHING_ROD)) {
            return;
        }
        FabricAuraSkills plugin = FabricAuraSkills.getInstance();
        if (plugin != null) {
            FabricSharpHook.onSwingMainHandRod(plugin, self);
        }
    }
}
