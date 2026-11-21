package dev.granite.graniteskills.fabric.mixin;

import dev.granite.graniteskills.api.item.ModifierType;
import dev.granite.graniteskills.common.config.Option;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import dev.granite.graniteskills.fabric.FabricUser;
import dev.granite.graniteskills.fabric.item.FabricSkillsItemReader;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Prediction;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Last-resort armor gate. Prefer {@link EquippableArmorRequirementMixin} /
 * {@link ArmorSlotRequirementMixin} so the stack never leaves the inventory first.
 * If {@code setItemSlot} still runs with a failing piece, restore the stack instead of
 * destroying it by cancelling after the hand was already emptied.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityArmorRequirementMixin {

    @Inject(method = "setItemSlot", at = @At("HEAD"), cancellable = true)
    private void graniteskills$cancelArmorIfRequirementsFail(EquipmentSlot slot, ItemStack stack, CallbackInfo ci) {
        if (stack.isEmpty()) {
            return;
        }
        if (slot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR) {
            return;
        }
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof ServerPlayer player)) {
            return;
        }
        FabricGraniteSkills plugin = FabricGraniteSkills.getInstance();
        if (plugin == null || !plugin.isRuntimeReady()) {
            return;
        }
        if (plugin.getWorldManager().isDisabledWorld(FabricUser.worldNameCompat(player))) {
            return;
        }
        if (!plugin.configBoolean(Option.REQUIREMENT_ENABLED)) {
            return;
        }
        if (!plugin.configBoolean(Option.REQUIREMENT_ARMOR_PREVENT_ARMOR_EQUIP)) {
            return;
        }
        if (FabricSkillsItemReader.meetsRequirements(plugin, player, stack, ModifierType.ARMOR)) {
            return;
        }
        // Cancel without copying. Restore only when this exact stack instance is no longer
        // stored anywhere on the player. Component equality is not ownership: a different stack
        // with the same components can sit in a hand, and the incoming stack can still live in
        // another slot.
        ci.cancel();
        if (!stackStillOwned(player, stack)) {
            player.getInventory().placeItemBackInInventory(stack.copy(), Prediction.SERVER_ONLY);
        }
    }

    private static boolean stackStillOwned(ServerPlayer player, ItemStack stack) {
        if (player.getMainHandItem() == stack || player.getOffhandItem() == stack) {
            return true;
        }
        var inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (inventory.getItem(slot) == stack) {
                return true;
            }
        }
        for (EquipmentSlot equipment : EquipmentSlot.values()) {
            if (player.getItemBySlot(equipment) == stack) {
                return true;
            }
        }
        return false;
    }
}
