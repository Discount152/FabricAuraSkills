package dev.granite.graniteskills.fabric.mixin;

import dev.granite.graniteskills.fabric.FabricBrewingXpHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 26.3 moved potion results into a private {@code doBrew(ServerLevel, BlockPos, BrewingStandBlockEntity)}.
 */
@Mixin(BrewingStandBlockEntity.class)
public abstract class BrewingStandBlockEntityMixin {

    @Shadow
    protected abstract NonNullList<ItemStack> getItems();

    @Inject(
            method = "doBrew(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BrewingStandBlockEntity;)V",
            at = @At("HEAD")
    )
    private static void graniteskills$brewHead(ServerLevel world, BlockPos pos, BrewingStandBlockEntity stand, CallbackInfo ci) {
        FabricBrewingXpHandler.onCraftHead(world, pos, ((BrewingStandBlockEntityMixin) (Object) stand).getItems());
    }

    @Inject(
            method = "doBrew(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BrewingStandBlockEntity;)V",
            at = @At("RETURN")
    )
    private static void graniteskills$brewReturn(ServerLevel world, BlockPos pos, BrewingStandBlockEntity stand, CallbackInfo ci) {
        FabricBrewingXpHandler.onCraftReturn(world, pos, ((BrewingStandBlockEntityMixin) (Object) stand).getItems());
    }
}
