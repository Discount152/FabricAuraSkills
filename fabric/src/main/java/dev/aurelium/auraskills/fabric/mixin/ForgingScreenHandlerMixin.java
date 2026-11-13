package dev.aurelium.auraskills.fabric.mixin;

import dev.aurelium.auraskills.fabric.FabricAnvilXpHandler;
import dev.aurelium.auraskills.fabric.ability.FabricForgingAbilities;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Mixin AnvilScreenHandler: onTakeOutput is abstract on ForgingScreenHandler (no injectable bytecode).
@Mixin(AnvilMenu.class)
public abstract class ForgingScreenHandlerMixin {

    @Inject(method = "onTake", at = @At("HEAD"))
    private void auraskills$forgingTakeOutput(Player player, ItemStack stack, CallbackInfo ci) {
        if (player.level().isClientSide() || !(player instanceof ServerPlayer sp)) {
            return;
        }
        FabricAnvilXpHandler.onTakeOutput(sp, (ItemCombinerMenu) (Object) this);
    }

    @Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
    private void auraskills$anvilMaster(Player player, boolean present, CallbackInfoReturnable<Boolean> cir) {
        if (!present || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        int cost = ((AnvilMenu) (Object) this).getCost();
        if (FabricForgingAbilities.allowExpensiveAnvil(serverPlayer, cost)) {
            cir.setReturnValue(true);
        }
    }
}
