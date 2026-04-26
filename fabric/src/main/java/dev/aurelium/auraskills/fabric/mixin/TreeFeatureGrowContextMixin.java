package dev.aurelium.auraskills.fabric.mixin;

import dev.aurelium.auraskills.fabric.region.FabricTreeGrowContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.gen.feature.TreeFeature;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Bukkit {@code StructureGrowEvent}-style context for {@code check_replace} tree grow height.
 *
 * <p><strong>Sprint 76B:</strong> Must target {@link TreeFeature}, not {@link
 * net.minecraft.world.gen.feature.Feature}. The base {@code Feature#generate(FeatureContext)} is
 * <em>abstract</em> — it has no bytecode body in {@code Feature.class}, so {@code @Inject} at
 * {@code HEAD}/{@code RETURN} there fails during mixin apply ({@code insnNode == null}). {@link
 * TreeFeature} implements {@code generate} with a real body (intermediary {@code method_13151}).
 */
@Mixin(TreeFeature.class)
public class TreeFeatureGrowContextMixin {

    /**
     * Explicit JVM descriptor: {@link TreeFeature} also declares another {@code generate} overload
     * ({@code StructureWorldAccess}, etc.); we only hook the {@code FeatureContext} entry used for
     * configured world generation.
     */
    @Inject(
            method = "generate(Lnet/minecraft/world/gen/feature/util/FeatureContext;)Z",
            at = @At("HEAD"))
    private void auraskills$treeFeatureGenHead(
            FeatureContext<TreeFeatureConfig> context, CallbackInfoReturnable<Boolean> cir) {
        if (context.getWorld().isClient() || !(context.getWorld() instanceof ServerWorld)) {
            return;
        }
        FabricTreeGrowContext.begin(context.getOrigin().getY());
    }

    @Inject(
            method = "generate(Lnet/minecraft/world/gen/feature/util/FeatureContext;)Z",
            at = @At("RETURN"))
    private void auraskills$treeFeatureGenReturn(
            FeatureContext<TreeFeatureConfig> context, CallbackInfoReturnable<Boolean> cir) {
        FabricTreeGrowContext.end();
    }
}
