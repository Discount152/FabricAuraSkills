package dev.aurelium.auraskills.fabric.mixin;

import dev.aurelium.auraskills.fabric.FabricExternalBlockBreakXp;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Sprint 146: observes every player-attributed block drop so multi-break mods that cancel
 * {@code PlayerBlockBreakEvents} (Ore Harvester, Tree Harvester, vein miners) still award XP per
 * block. Read-only — never cancels, never mutates — so the harvesting mod is unaffected.
 */
@Mixin(Block.class)
public abstract class BlockDropStacksMultiBreakMixin {

    @Inject(
            method = "dropStacks(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;"
                    + "Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/entity/BlockEntity;"
                    + "Lnet/minecraft/entity/Entity;Lnet/minecraft/item/ItemStack;)V",
            at = @At("HEAD"))
    private static void auraskills$captureExternalBreak(BlockState state, World world, BlockPos pos,
            BlockEntity blockEntity, Entity entity, ItemStack tool, CallbackInfo ci) {
        FabricExternalBlockBreakXp.capture(state, world, pos, entity);
    }
}
