package dev.aurelium.auraskills.fabric.mixin;

import dev.aurelium.auraskills.fabric.ability.FabricFarmingAbilities;
import net.minecraft.block.BlockState;
import net.minecraft.block.CactusBlock;
import net.minecraft.block.CocoaBlock;
import net.minecraft.block.CaveVinesBodyBlock;
import net.minecraft.block.CaveVinesHeadBlock;
import net.minecraft.block.CropBlock;
import net.minecraft.block.KelpBlock;
import net.minecraft.block.KelpPlantBlock;
import net.minecraft.block.NetherWartBlock;
import net.minecraft.block.StemBlock;
import net.minecraft.block.SugarCaneBlock;
import net.minecraft.block.SweetBerryBushBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Bukkit {@code BlockGrowEvent} with {@code Ageable} new state — natural {@code randomTick} growth for
 * {@link dev.aurelium.auraskills.api.ability.Abilities#GROWTH_AURA} on blocks that use an "age" property
 * and override {@link net.minecraft.block.Block#randomTick}.
 *
 * <p>Batch 1 (Sprint 23): {@link CropBlock}, {@link NetherWartBlock}. Batch 2 (Sprint 24) adds
 * cactus, sugar cane, cocoa, pumpkin/melon stem, sweet berry bush, kelp, cave vines, plus
 * crops/nether wart from Sprint 23.
 * <p><strong>Not mixed in:</strong> bamboo and chorus (growth often places or replaces non–single-age blocks);
 * most bonemeal paths; tree/sapling structure growth. See {@code SPRINT_24_*} docs.
 */
@Mixin({
        CropBlock.class, NetherWartBlock.class, CactusBlock.class, SugarCaneBlock.class, CocoaBlock.class, StemBlock.class, SweetBerryBushBlock.class,
        KelpBlock.class, KelpPlantBlock.class, CaveVinesHeadBlock.class, CaveVinesBodyBlock.class
})
public class FarmingAgeableRandomTickGrowthAuraMixin {

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
