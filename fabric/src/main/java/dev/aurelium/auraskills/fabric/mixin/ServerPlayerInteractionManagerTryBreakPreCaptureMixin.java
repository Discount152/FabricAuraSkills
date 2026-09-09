package dev.aurelium.auraskills.fabric.mixin;

import dev.aurelium.auraskills.fabric.FabricCropPreUseCapture;
import dev.aurelium.auraskills.fabric.FabricExternalBlockBreakXp;
import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Sprint 141: probe + capture pre-break crop/age state before mutation/replant mods. */
@Mixin(ServerPlayerInteractionManager.class)
public abstract class ServerPlayerInteractionManagerTryBreakPreCaptureMixin {

    @Shadow
    @Final
    protected ServerWorld world;

    @Shadow
    protected ServerPlayerEntity player;

    @Inject(method = "tryBreakBlock", at = @At("HEAD"))
    private void auraskills$capturePreBreakState(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (world == null || world.isClient()) {
            return;
        }
        BlockState st = world.getBlockState(pos);
        if (st == null || st.isAir()) {
            return;
        }
        // Sprint 146: arm here, not in PlayerBlockBreakEvents.BEFORE — that event short-circuits on
        // the first listener returning false, so a vein-mining mod registered ahead of us would hide
        // the break entirely.
        FabricExternalBlockBreakXp.arm(player, world, pos);
        FabricCropPreUseCapture.record(
                player,
                world,
                pos,
                st,
                "try_break_head",
                Hand.MAIN_HAND,
                player.getMainHandStack());
    }
}