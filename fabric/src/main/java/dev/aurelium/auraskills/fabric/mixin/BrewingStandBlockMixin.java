package dev.aurelium.auraskills.fabric.mixin;

import dev.aurelium.auraskills.fabric.FabricBrewingOwnerTracker;
import net.minecraft.block.BlockState;
import net.minecraft.block.BrewingStandBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Bukkit {@code BrewingLeveler#onInventoryOpen}: claim stand owner when a player uses the brewing stand (opens GUI).
 */
@Mixin(BrewingStandBlock.class)
public abstract class BrewingStandBlockMixin {

    @Inject(method = "onUse", at = @At("HEAD"))
    private void auraskills$claimBrewingOwner(
            BlockState state,
            World world,
            BlockPos pos,
            PlayerEntity player,
            BlockHitResult hit,
            CallbackInfoReturnable<ActionResult> cir) {
        if (world.isClient() || !(world instanceof ServerWorld sw) || !(player instanceof ServerPlayerEntity sp)) {
            return;
        }
        FabricBrewingOwnerTracker.claimIfAbsent(sw, pos, sp.getUuid());
    }
}
