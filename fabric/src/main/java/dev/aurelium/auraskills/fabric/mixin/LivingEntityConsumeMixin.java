package dev.aurelium.auraskills.fabric.mixin;

import dev.aurelium.auraskills.fabric.FabricItemConsumeHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityConsumeMixin {

    /**
     * {@code LivingEntity#consumeItem} is the vanilla consumption <strong>commit</strong> for the active item
     * (post–use-duration), not the start of “using” the item. Only {@link ServerPlayerEntity} is handled; mobs and
     * other {@link LivingEntity} types are ignored.
     */
    @Inject(method = "consumeItem", at = @At("HEAD"))
    private void auraskills$onConsumeItem(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self.getEntityWorld().isClient() || !(self instanceof ServerPlayerEntity player)) {
            return;
        }
        ItemStack active = self.getActiveItem();
        if (active.isEmpty()) {
            return;
        }
        FabricItemConsumeHandler.onConsumeItemCommit(player, active.copy());
    }
}
