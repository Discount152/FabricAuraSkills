package dev.granite.graniteskills.fabric.mixin;

import dev.granite.graniteskills.fabric.region.FabricPlacedBlocks;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.FallingBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FallingBlockEntity.class)
public abstract class FallingBlockPlacedMixin {

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"))
    private void graniteskills$movePlacedWhenFallingBlockLands(CallbackInfo callback) {
        FallingBlockEntity self = (FallingBlockEntity) (Object) this;
        if (!(self.level() instanceof ServerLevel server)) {
            return;
        }
        FabricPlacedBlocks.move(server, self.getStartPos(), self.blockPosition());
    }
}
