package dev.aurelium.auraskills.fabric.mixin;

import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.FabricSharpHook;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntitySharpHookMixin {

    @Inject(method = "swingHand(Lnet/minecraft/util/Hand;)V", at = @At("HEAD"))
    private void auraskills$sharpHookOnSwing(Hand hand, CallbackInfo ci) {
        if (hand != Hand.MAIN_HAND) {
            return;
        }
        ServerPlayerEntity self = (ServerPlayerEntity) (Object) this;
        if (!self.getMainHandStack().isOf(Items.FISHING_ROD)) {
            return;
        }
        FabricAuraSkills plugin = FabricAuraSkills.getInstance();
        if (plugin != null) {
            FabricSharpHook.onSwingMainHandRod(plugin, self);
        }
    }
}
