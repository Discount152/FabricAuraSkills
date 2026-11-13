package dev.granite.graniteskills.fabric.mixin;

import dev.granite.graniteskills.fabric.FabricBrewingXpHandler;
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
    private boolean graniteskillsBrewingTakeoutTracking;

    @Unique
    private int graniteskillsBrewingTakeoutSlot = -1;

    @Unique
    private ItemStack graniteskillsBrewingTakeoutBefore = ItemStack.EMPTY;

    @Inject(method = "clicked", at = @At("HEAD"))
    private void graniteskills$brewingTakeoutHead(
            int slotIndex, int button, ContainerInput actionType, Player player, CallbackInfo ci) {
        if (!((Object) this instanceof BrewingStandMenu handler)) {
            return;
        }
        graniteskillsBrewingTakeoutTracking = false;
        graniteskillsBrewingTakeoutSlot = -1;
        graniteskillsBrewingTakeoutBefore = ItemStack.EMPTY;
        if (slotIndex < 0 || slotIndex > 2) {
            return;
        }
        if (!(player instanceof ServerPlayer) || player.level().isClientSide()) {
            return;
        }
        graniteskillsBrewingTakeoutTracking = true;
        graniteskillsBrewingTakeoutSlot = slotIndex;
        graniteskillsBrewingTakeoutBefore = handler.getSlot(slotIndex).getItem().copy();
    }

    @Inject(method = "clicked", at = @At("RETURN"))
    private void graniteskills$brewingTakeoutReturn(
            int slotIndex, int button, ContainerInput actionType, Player player, CallbackInfo ci) {
        try {
            if (!graniteskillsBrewingTakeoutTracking || !(player instanceof ServerPlayer sp)) {
                return;
            }
            if (!((Object) this instanceof BrewingStandMenu handler)) {
                return;
            }
            if (slotIndex != graniteskillsBrewingTakeoutSlot) {
                return;
            }
            FabricBrewingXpHandler.onPotionSlotClickFinish(
                    sp, handler, slotIndex, actionType, graniteskillsBrewingTakeoutBefore);
        } finally {
            graniteskillsBrewingTakeoutTracking = false;
            graniteskillsBrewingTakeoutSlot = -1;
            graniteskillsBrewingTakeoutBefore = ItemStack.EMPTY;
        }
    }
}
