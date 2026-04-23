package dev.aurelium.auraskills.fabric.mixin;

import dev.aurelium.auraskills.fabric.FabricAnvilTakeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * {@code onTake} is declared on {@link ForgingScreenHandler}, not on {@link AnvilScreenHandler}’s class file in a way
 * Loom can remap for an anvil-only mixin (see Sprint 16 build). Target the forging base, then branch
 * <strong>only</strong> for the anvil — smithing and other inheritors are unaffected.
 * <p>
 * Bukkit {@code AnvilLeveler} reads left/right and repair cost before input consumption; we snapshot at
 * {@code onTake} HEAD for the result slot, before that method mutates input stacks.
 */
@Mixin(ForgingScreenHandler.class)
public class ForgingScreenHandlerAnvilOnTakeMixin {

    @Inject(method = "onTake", at = @At("HEAD"))
    private void auraskills$anvilOnTakeHead(PlayerEntity player, int slot, CallbackInfo ci) {
        if (!((Object) this instanceof AnvilScreenHandler anvil)) {
            return;
        }
        if (player.getEntityWorld().isClient() || !(player instanceof ServerPlayerEntity)) {
            return;
        }
        if (slot != AnvilScreenHandler.OUTPUT_ID) {
            return;
        }
        ItemStack left = anvil.getSlot(AnvilScreenHandler.INPUT_1_ID).getStack();
        ItemStack right = anvil.getSlot(AnvilScreenHandler.INPUT_2_ID).getStack();
        int levelCost = anvil.getLevelCost();
        FabricAnvilTakeContext.setPendingForOutputTake(new FabricAnvilTakeContext.Pending(left, right, levelCost));
    }

    @Inject(method = "onTake", at = @At("RETURN"))
    private void auraskills$anvilOnTakeReturn(PlayerEntity player, int slot, CallbackInfo ci) {
        if (!((Object) this instanceof AnvilScreenHandler)) {
            return;
        }
        if (slot == AnvilScreenHandler.OUTPUT_ID) {
            FabricAnvilTakeContext.clear();
        }
    }
}
