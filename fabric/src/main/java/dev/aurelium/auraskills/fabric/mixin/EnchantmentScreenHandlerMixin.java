package dev.aurelium.auraskills.fabric.mixin;

import dev.aurelium.auraskills.fabric.FabricEnchantingXpHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentMenu.class)
public abstract class EnchantmentScreenHandlerMixin {

    @Unique
    private ItemStack auraskillsEnchantItemBefore = ItemStack.EMPTY;

    @Unique
    private int auraskillsXpLevelBefore;

    @Inject(method = "clickMenuButton", at = @At("HEAD"))
    private void auraskills$enchantHead(Player player, int id, CallbackInfoReturnable<Boolean> cir) {
        if (id < 0 || id > 2) {
            return;
        }
        EnchantmentMenu self = (EnchantmentMenu) (Object) this;
        auraskillsEnchantItemBefore = self.getSlot(0).getItem().copy();
        auraskillsXpLevelBefore = player.experienceLevel;
    }

    @Inject(method = "clickMenuButton", at = @At("RETURN"))
    private void auraskills$enchantReturn(Player player, int id, CallbackInfoReturnable<Boolean> cir) {
        try {
            if (!cir.getReturnValue()) {
                return;
            }
            if (player.level().isClientSide() || !(player instanceof ServerPlayer sp)) {
                return;
            }
            if (id < 0 || id > 2) {
                return;
            }
            FabricEnchantingXpHandler.onEnchantSuccess(sp, (EnchantmentMenu) (Object) this, id,
                    auraskillsEnchantItemBefore, auraskillsXpLevelBefore);
        } finally {
            auraskillsEnchantItemBefore = ItemStack.EMPTY;
        }
    }
}
