package dev.granite.graniteskills.fabric.mixin;

import dev.granite.graniteskills.api.item.ModifierType;
import dev.granite.graniteskills.common.config.Option;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import dev.granite.graniteskills.fabric.FabricUser;
import dev.granite.graniteskills.fabric.item.FabricSkillsItemReader;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Blocks right-click / swap equip before the hand is emptied (avoids item loss from cancelling
 * {@code setItemSlot} after the stack has already left the inventory).
 */
@Mixin(Equippable.class)
public abstract class EquippableArmorRequirementMixin {

    @Inject(method = "swapWithEquipmentSlot", at = @At("HEAD"), cancellable = true)
    private void graniteskills$blockSwapEquip(ItemStack stack, Player player, CallbackInfoReturnable<InteractionResult> cir) {
        if (shouldBlock(player, stack)) {
            cir.setReturnValue(InteractionResult.FAIL);
        }
    }

    @Inject(method = "equipOnTarget", at = @At("HEAD"), cancellable = true)
    private void graniteskills$blockTargetEquip(Player player, LivingEntity target, ItemStack stack,
            CallbackInfoReturnable<InteractionResult> cir) {
        if (target instanceof ServerPlayer serverPlayer && shouldBlock(serverPlayer, stack)) {
            cir.setReturnValue(InteractionResult.FAIL);
        }
    }

    private static boolean shouldBlock(Player player, ItemStack stack) {
        if (!(player instanceof ServerPlayer serverPlayer) || stack == null || stack.isEmpty()) {
            return false;
        }
        FabricGraniteSkills plugin = FabricGraniteSkills.getInstance();
        if (plugin == null || !plugin.isRuntimeReady()) {
            return false;
        }
        if (plugin.getWorldManager().isDisabledWorld(FabricUser.worldNameCompat(serverPlayer))) {
            return false;
        }
        if (!plugin.configBoolean(Option.REQUIREMENT_ENABLED)
                || !plugin.configBoolean(Option.REQUIREMENT_ARMOR_PREVENT_ARMOR_EQUIP)) {
            return false;
        }
        return !FabricSkillsItemReader.meetsRequirements(plugin, serverPlayer, stack, ModifierType.ARMOR);
    }
}
