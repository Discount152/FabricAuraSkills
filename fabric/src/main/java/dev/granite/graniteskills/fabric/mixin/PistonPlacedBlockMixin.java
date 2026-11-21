package dev.granite.graniteskills.fabric.mixin;

import dev.granite.graniteskills.fabric.region.FabricPlacedBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PistonBaseBlock.class)
public abstract class PistonPlacedBlockMixin {

    @Inject(method = "moveBlocks", at = @At("HEAD"))
    private void graniteskills$movePlacedRecords(Level level, BlockPos pistonPos, Direction direction, boolean extending,
            CallbackInfoReturnable<Boolean> callback) {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        PistonStructureResolver resolver = new PistonStructureResolver(level, pistonPos, direction, extending);
        if (!resolver.resolve()) {
            return;
        }
        for (BlockPos from : resolver.getToDestroy()) {
            FabricPlacedBlocks.clear(server, from);
        }
        for (BlockPos from : resolver.getToPush()) {
            FabricPlacedBlocks.move(server, from, from.relative(resolver.getPushDirection()));
        }
    }
}
