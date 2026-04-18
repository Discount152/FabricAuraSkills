package dev.aurelium.auraskills.fabric.mixin;

import dev.aurelium.auraskills.fabric.FabricAnvilXpHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Mixin AnvilScreenHandler: onTakeOutput is abstract on ForgingScreenHandler (no injectable bytecode).
@Mixin(AnvilScreenHandler.class)
public abstract class ForgingScreenHandlerMixin {

    @Inject(method = "onTakeOutput", at = @At("HEAD"))
    private void auraskills$forgingTakeOutput(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
        if (player.getEntityWorld().isClient() || !(player instanceof ServerPlayerEntity sp)) {
            return;
        }
        FabricAnvilXpHandler.onTakeOutput(sp, (ForgingScreenHandler) (Object) this);
    }
}
