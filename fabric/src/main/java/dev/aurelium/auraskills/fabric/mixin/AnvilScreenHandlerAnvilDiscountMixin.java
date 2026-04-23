package dev.aurelium.auraskills.fabric.mixin;

import dev.aurelium.auraskills.fabric.FabricAnvilDiscount;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.Property;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Bukkit {@code PrepareAnvilEvent} parity: apply the {@code anvil_discount} trait after
 * {@link AnvilScreenHandler#updateResult()} has set the vanilla level cost, so the shown and
 * charged level cost (including Forging XP in {@code FabricAnvilTakeContext} / {@code getLevelCost})
 * stay in sync.
 */
@Mixin(AnvilScreenHandler.class)
public abstract class AnvilScreenHandlerAnvilDiscountMixin {

    @Shadow
    @Final
    private Property levelCost;
    @Shadow
    @Final
    protected PlayerEntity player;

    @Inject(method = "updateResult", at = @At("TAIL"))
    private void auraskills$applyAnvilTraitDiscount(CallbackInfo ci) {
        if (this.player.getEntityWorld().isClient()) {
            return;
        }
        if (!(this.player instanceof ServerPlayerEntity sp)) {
            return;
        }
        FabricAnvilDiscount.afterUpdateResult(this.levelCost, sp);
    }
}
