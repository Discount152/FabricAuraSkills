package dev.granite.graniteskills.fabric.mixin;

import dev.granite.graniteskills.fabric.FabricXpAwardGuard;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public abstract class LevelXpOccurrenceMixin {

    @Inject(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z",
            at = @At("RETURN"))
    private void graniteskills$noteXpOccurrence(BlockPos pos, BlockState state, int flags, int recursion,
            CallbackInfoReturnable<Boolean> callback) {
        if (!Boolean.TRUE.equals(callback.getReturnValue())) {
            return;
        }
        Level level = (Level) (Object) this;
        if (level.isClientSide()) {
            return;
        }
        FabricXpAwardGuard.noteBlockChanged(level.dimension().identifier().toString(), pos);
    }
}
