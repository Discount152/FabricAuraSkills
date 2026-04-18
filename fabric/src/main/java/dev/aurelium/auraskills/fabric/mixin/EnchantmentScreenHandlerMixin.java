package dev.aurelium.auraskills.fabric.mixin;

import dev.aurelium.auraskills.fabric.FabricEnchantingXpHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.EnchantmentScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentScreenHandler.class)
public abstract class EnchantmentScreenHandlerMixin {

    @Unique
    private ItemStack auraskills$enchantItemBefore = ItemStack.EMPTY;

    @Unique
    private int auraskills$xpLevelBefore;

    @Inject(method = "onButtonClick", at = @At("HEAD"))
    private void auraskills$enchantHead(PlayerEntity player, int id, CallbackInfoReturnable<Boolean> cir) {
        if (id < 0 || id > 2) {
            return;
        }
        EnchantmentScreenHandler self = (EnchantmentScreenHandler) (Object) this;
        auraskills$enchantItemBefore = self.getSlot(0).getStack().copy();
        auraskills$xpLevelBefore = player.experienceLevel;
    }

    @Inject(method = "onButtonClick", at = @At("RETURN"))
    private void auraskills$enchantReturn(PlayerEntity player, int id, CallbackInfoReturnable<Boolean> cir) {
        try {
            if (!cir.getReturnValue()) {
                return;
            }
            if (player.getEntityWorld().isClient() || !(player instanceof ServerPlayerEntity sp)) {
                return;
            }
            if (id < 0 || id > 2) {
                return;
            }
            FabricEnchantingXpHandler.onEnchantSuccess(sp, (EnchantmentScreenHandler) (Object) this, id,
                    auraskills$enchantItemBefore, auraskills$xpLevelBefore);
        } finally {
            auraskills$enchantItemBefore = ItemStack.EMPTY;
        }
    }
}
