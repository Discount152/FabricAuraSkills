package dev.aurelium.auraskills.fabric.mixin;

import dev.aurelium.auraskills.fabric.region.FabricTreeGrowContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.TreeFeature;
import net.minecraft.world.gen.feature.util.FeatureContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Feature.class)
public class TreeFeatureGrowContextMixin {

    @Inject(method = "generate", at = @At("HEAD"))
    private void auraskills$treeFeatureGenHead(FeatureContext<?> context, CallbackInfoReturnable<Boolean> cir) {
        if (!((Object) this instanceof TreeFeature)) {
            return;
        }
        if (context.getWorld().isClient() || !(context.getWorld() instanceof ServerWorld)) {
            return;
        }
        FabricTreeGrowContext.begin(context.getOrigin().getY());
    }

    @Inject(method = "generate", at = @At("RETURN"))
    private void auraskills$treeFeatureGenReturn(CallbackInfoReturnable<Boolean> cir) {
        if (!((Object) this instanceof TreeFeature)) {
            return;
        }
        FabricTreeGrowContext.end();
    }
}
