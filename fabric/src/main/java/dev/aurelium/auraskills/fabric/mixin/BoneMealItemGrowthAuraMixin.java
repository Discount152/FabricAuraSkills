package dev.aurelium.auraskills.fabric.mixin;

import dev.aurelium.auraskills.fabric.ability.FabricFarmingAbilities;
import net.minecraft.block.BlockState;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Bukkit {@code BlockFertilizeEvent} / success path for block growth with {@code Ageable} new data:
 * if bone meal advances the {@code "age"} at the <strong>target</strong> position with the same
 * block class, run the same {@code GROWTH_AURA} pipeline as {@code FarmingAgeableRandomTickGrowthAuraMixin}.
 * Non-age effects (dirt to grass, full tree) see {@code start.getBlock() != end.getBlock()} and skip.
 */
@Mixin(BoneMealItem.class)
public class BoneMealItemGrowthAuraMixin {

    @Unique
    private static final ThreadLocal<BlockState> AURASKILLS$PRE = new ThreadLocal<>();
    @Unique
    private static final ThreadLocal<BlockPos> AURASKILLS$POS = new ThreadLocal<>();
    @Unique
    private static final ThreadLocal<ServerWorld> AURASKILLS$W = new ThreadLocal<>();

    @Inject(method = "useOnBlock", at = @At("HEAD"))
    private void auraskills$boneMealPre(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        World w = context.getWorld();
        if (w.isClient()) {
            return;
        }
        if (!(w instanceof ServerWorld sw)) {
            return;
        }
        BlockPos pos = context.getBlockPos();
        AURASKILLS$W.set(sw);
        AURASKILLS$POS.set(pos);
        AURASKILLS$PRE.set(w.getBlockState(pos));
    }

    @Inject(method = "useOnBlock", at = @At("TAIL"))
    private void auraskills$boneMealPost(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        try {
            ServerWorld world = AURASKILLS$W.get();
            BlockPos pos = AURASKILLS$POS.get();
            BlockState start = AURASKILLS$PRE.get();
            if (world == null || pos == null || start == null) {
                return;
            }
            BlockState end = world.getBlockState(pos);
            FabricFarmingAbilities.onGrowthAuraAfterAgeAtPos(world, pos, start, end);
        } finally {
            AURASKILLS$PRE.remove();
            AURASKILLS$POS.remove();
            AURASKILLS$W.remove();
        }
    }
}
