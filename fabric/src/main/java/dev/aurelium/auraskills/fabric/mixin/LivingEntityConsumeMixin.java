package dev.aurelium.auraskills.fabric.mixin;

import dev.aurelium.auraskills.fabric.FabricItemConsumeHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityConsumeMixin {

    @Inject(method = "completeUsingItem", at = @At("HEAD"))
    private void auraskills$onConsumeItem(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self.level().isClientSide() || !(self instanceof ServerPlayer player)) {
            return;
        }
        ItemStack active = self.getUseItem();
        if (active.isEmpty()) {
            return;
        }
        FabricItemConsumeHandler.onConsumeStarting(player, active.copy());
    }
}
