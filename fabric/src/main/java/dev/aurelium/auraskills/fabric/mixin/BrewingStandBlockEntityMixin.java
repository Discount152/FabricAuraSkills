package dev.aurelium.auraskills.fabric.mixin;

import dev.aurelium.auraskills.fabric.FabricBrewingXpHandler;
import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BrewingStandBlockEntity.class)
public abstract class BrewingStandBlockEntityMixin {

    @Inject(method = "craft", at = @At("HEAD"))
    private static void auraskills$brewHead(World world, BlockPos pos, DefaultedList<net.minecraft.item.ItemStack> slots, CallbackInfo ci) {
        if (world.isClient() || !(world instanceof ServerWorld sw)) {
            return;
        }
        FabricBrewingXpHandler.onCraftHead(sw, pos, slots);
    }

    @Inject(method = "craft", at = @At("RETURN"))
    private static void auraskills$brewReturn(World world, BlockPos pos, DefaultedList<net.minecraft.item.ItemStack> slots, CallbackInfo ci) {
        if (world.isClient() || !(world instanceof ServerWorld sw)) {
            return;
        }
        FabricBrewingXpHandler.onCraftReturn(sw, pos, slots);
    }
}
