package dev.granite.graniteskills.fabric.mixin;

import dev.granite.graniteskills.fabric.FabricAnvilXpHandler;
import dev.granite.graniteskills.fabric.trait.FabricAnvilDiscount;
import dev.granite.graniteskills.fabric.ability.FabricForgingAbilities;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;

// Mixin AnvilScreenHandler: onTakeOutput is abstract on ForgingScreenHandler (no injectable bytecode).
@Mixin(AnvilMenu.class)
public abstract class ForgingScreenHandlerMixin {

    @Unique
    private static final Field PLAYER_FIELD = findPlayerField();

    @Inject(method = "onTake", at = @At("HEAD"))
    private void graniteskills$forgingTakeOutput(Player player, ItemStack stack, CallbackInfo ci) {
        if (player.level().isClientSide() || !(player instanceof ServerPlayer sp)) {
            return;
        }
        FabricAnvilXpHandler.onTakeOutput(sp, (ItemCombinerMenu) (Object) this);
    }

    @Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
    private void graniteskills$anvilMaster(Player player, boolean present, CallbackInfoReturnable<Boolean> cir) {
        if (!present || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        FabricAnvilDiscount.apply(serverPlayer, (AnvilMenu) (Object) this);
        int cost = ((AnvilMenu) (Object) this).getCost();
        if (FabricForgingAbilities.allowExpensiveAnvil(serverPlayer, cost)) {
            cir.setReturnValue(true);
        }
    }

    /**
     * Bukkit Repairing: after vanilla builds the anvil result, restore additional durability
     * when the second slot is the tool's raw material.
     */
    @Inject(method = "createResult", at = @At("RETURN"))
    private void graniteskills$repairing(CallbackInfo ci) {
        AnvilMenu self = (AnvilMenu) (Object) this;
        Player menuPlayer = graniteskills$menuPlayer(self);
        if (menuPlayer == null || menuPlayer.level().isClientSide() || !(menuPlayer instanceof ServerPlayer serverPlayer)) {
            return;
        }
        ItemStack first = self.getSlot(AnvilMenu.INPUT_SLOT).getItem();
        ItemStack second = self.getSlot(AnvilMenu.ADDITIONAL_SLOT).getItem();
        ItemStack result = self.getSlot(AnvilMenu.RESULT_SLOT).getItem();
        if (first.isEmpty() || second.isEmpty() || result.isEmpty() || !result.isDamageableItem()) {
            return;
        }
        int added = FabricForgingAbilities.computeRepairingAddedDurability(serverPlayer, first, second);
        if (added <= 0) {
            return;
        }
        int nextDamage = Math.max(0, result.getDamageValue() - added);
        result.setDamageValue(nextDamage);
    }

    @Unique
    private static Player graniteskills$menuPlayer(AnvilMenu menu) {
        if (PLAYER_FIELD == null) {
            return null;
        }
        try {
            Object value = PLAYER_FIELD.get(menu);
            return value instanceof Player player ? player : null;
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    @Unique
    private static Field findPlayerField() {
        try {
            Field field = ItemCombinerMenu.class.getDeclaredField("player");
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            return null;
        }
    }
}
