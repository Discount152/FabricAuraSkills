package dev.aurelium.auraskills.fabric.mixin;

import dev.aurelium.auraskills.fabric.region.FabricRegionBlockIntegrity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PistonHandler.class)
public class PistonHandlerRegionMixin {

    @Shadow
    @Final
    private World world;

    @Inject(method = "calculatePush", at = @At("TAIL"))
    private void auraskills$afterCalculatePush(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ()) {
            return;
        }
        FabricRegionBlockIntegrity.onPistonBlocksCalculated((PistonHandler) (Object) this, world);
    }
}
