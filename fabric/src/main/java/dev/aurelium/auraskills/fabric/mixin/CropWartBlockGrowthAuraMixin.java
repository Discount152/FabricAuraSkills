package dev.aurelium.auraskills.fabric.mixin;

import dev.aurelium.auraskills.fabric.ability.FabricFarmingAbilities;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.block.NetherWartBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Bukkit {@code BlockGrowEvent}–style growth for {@link CropBlock} and {@link NetherWartBlock}
 * to drive {@link dev.aurelium.auraskills.api.ability.Abilities#GROWTH_AURA}.
 */
@Mixin({CropBlock.class, NetherWartBlock.class})
public class CropWartBlockGrowthAuraMixin {

    @Unique
    private static final ThreadLocal<BlockState> AURASKILLS$GROW_PRE = new ThreadLocal<>();

    @Inject(method = "randomTick", at = @At("HEAD"))
    private void auraskills$captureStateBeforeRandomTick(
            BlockState state,
            ServerWorld world,
            BlockPos pos,
            Random random,
            CallbackInfo ci) {
        AURASKILLS$GROW_PRE.set(state);
    }

    @Inject(method = "randomTick", at = @At("TAIL"))
    private void auraskills$afterRandomTick(
            BlockState state,
            ServerWorld world,
            BlockPos pos,
            Random random,
            CallbackInfo ci) {
        try {
            BlockState start = AURASKILLS$GROW_PRE.get();
            if (start == null) {
                return;
            }
            BlockState end = world.getBlockState(pos);
            FabricFarmingAbilities.onNaturalAgingAfterRandomTick(world, pos, start, end);
        } finally {
            AURASKILLS$GROW_PRE.remove();
        }
    }
}
