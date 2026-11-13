package dev.granite.graniteskills.fabric.mixin;

import dev.granite.graniteskills.fabric.FabricBrewingOwnerTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BrewingStandBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Bukkit {@code BrewingLeveler#onInventoryOpen}: claim stand owner when a player uses the brewing stand (opens GUI).
 */
@Mixin(BrewingStandBlock.class)
public abstract class BrewingStandBlockMixin {

    @Inject(method = "useWithoutItem", at = @At("HEAD"))
    private void graniteskills$claimBrewingOwner(
            BlockState state,
            Level world,
            BlockPos pos,
            Player player,
            BlockHitResult hit,
            CallbackInfoReturnable<InteractionResult> cir) {
        if (world.isClientSide() || !(world instanceof ServerLevel sw) || !(player instanceof ServerPlayer sp)) {
            return;
        }
        FabricBrewingOwnerTracker.claimIfAbsent(sw, pos, sp.getUUID());
    }
}
