package dev.granite.graniteskills.fabric.mixin;

import dev.granite.graniteskills.fabric.ability.FabricForagingAbilities;
import dev.granite.graniteskills.fabric.ability.FabricMiningAbilities;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fabric port of Bukkit Hardened Armor ({@code PlayerItemDamageEvent} cancel on armor)
 * and Shredder (triple armor durability loss when hit by an axe).
 */
@Mixin(ItemStack.class)
public abstract class ItemStackHardenedArmorMixin {

    @ModifyVariable(
            method = "hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;)V",
            at = @At("HEAD"),
            argsOnly = true
    )
    private int graniteskills$shredderArmor(int amount, int originalAmount, LivingEntity entity, EquipmentSlot slot) {
        if (amount <= 0 || slot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR) {
            return amount;
        }
        LivingEntity attacker = entity.getLastHurtByMob();
        if (!(attacker instanceof ServerPlayer sp)) {
            return amount;
        }
        ItemStack self = (ItemStack) (Object) this;
        if (FabricForagingAbilities.applyShredder(sp, entity, self, Double.NaN)) {
            return amount * 3;
        }
        return amount;
    }

    @Inject(
            method = "hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void graniteskills$hardenedArmor(int amount, LivingEntity entity, EquipmentSlot slot, CallbackInfo ci) {
        if (amount <= 0 || slot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR) {
            return;
        }
        if (!(entity instanceof ServerPlayer player)) {
            return;
        }
        ItemStack self = (ItemStack) (Object) this;
        if (FabricMiningAbilities.shouldCancelArmorDamage(player, self)) {
            ci.cancel();
        }
    }
}
