package dev.aurelium.auraskills.fabric.mixin;

import dev.aurelium.auraskills.fabric.FabricCropPreUseCapture;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerCropPreUseMixin {

    @Inject(method = "interactBlock", at = @At("HEAD"))
    private void auraskills$capturePreUseBlockState(
            ServerPlayerEntity player,
            World world,
            ItemStack stack,
            Hand hand,
            BlockHitResult hitResult,
            CallbackInfoReturnable<ActionResult> cir) {
        if (world.isClient() || !(world instanceof ServerWorld sw)) {
            return;
        }
        BlockState st = world.getBlockState(hitResult.getBlockPos());
        FabricCropPreUseCapture.record(
                player,
                sw,
                hitResult.getBlockPos(),
                st,
                "interact_block_head",
                hand,
                stack);
    }
}
