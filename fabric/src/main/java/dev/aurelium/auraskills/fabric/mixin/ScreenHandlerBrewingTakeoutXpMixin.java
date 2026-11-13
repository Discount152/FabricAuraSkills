package dev.aurelium.auraskills.fabric.mixin;

import dev.aurelium.auraskills.fabric.FabricBrewingXpHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.BrewingStandMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * {@code TAKEOUT} brewing XP when a potion is taken from slots 0–2 (Paper {@code BrewingLeveler#onTakePotionOut}).
 * Targets {@link AbstractContainerMenu#clicked} like {@link ScreenHandlerGrindstoneXpMixin}.
 */
@Mixin(AbstractContainerMenu.class)
public abstract class ScreenHandlerBrewingTakeoutXpMixin {

    @Unique
    private boolean auraskillsBrewingTakeoutTracking;

    @Unique
    private int auraskillsBrewingTakeoutSlot = -1;

    @Unique
    private ItemStack auraskillsBrewingTakeoutBefore = ItemStack.EMPTY;

    @Inject(method = "clicked", at = @At("HEAD"))
    private void auraskills$brewingTakeoutHead(
            int slotIndex, int button, ContainerInput actionType, Player player, CallbackInfo ci) {
        if (!((Object) this instanceof BrewingStandMenu handler)) {
            return;
        }
        auraskillsBrewingTakeoutTracking = false;
        auraskillsBrewingTakeoutSlot = -1;
        auraskillsBrewingTakeoutBefore = ItemStack.EMPTY;
        if (slotIndex < 0 || slotIndex > 2) {
            return;
        }
        if (!(player instanceof ServerPlayer) || player.level().isClientSide()) {
            return;
        }
        auraskillsBrewingTakeoutTracking = true;
        auraskillsBrewingTakeoutSlot = slotIndex;
        auraskillsBrewingTakeoutBefore = handler.getSlot(slotIndex).getItem().copy();
    }

    @Inject(method = "clicked", at = @At("RETURN"))
    private void auraskills$brewingTakeoutReturn(
            int slotIndex, int button, ContainerInput actionType, Player player, CallbackInfo ci) {
        try {
            if (!auraskillsBrewingTakeoutTracking || !(player instanceof ServerPlayer sp)) {
                return;
            }
            if (!((Object) this instanceof BrewingStandMenu handler)) {
                return;
            }
            if (slotIndex != auraskillsBrewingTakeoutSlot) {
                return;
            }
            FabricBrewingXpHandler.onPotionSlotClickFinish(
                    sp, handler, slotIndex, actionType, auraskillsBrewingTakeoutBefore);
        } finally {
            auraskillsBrewingTakeoutTracking = false;
            auraskillsBrewingTakeoutSlot = -1;
            auraskillsBrewingTakeoutBefore = ItemStack.EMPTY;
        }
    }
}
