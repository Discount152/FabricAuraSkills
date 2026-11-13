package dev.granite.graniteskills.fabric.mixin;

import dev.granite.graniteskills.fabric.FabricChargedShot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BowItem.class)
public abstract class BowItemChargedShotMixin {

    @Inject(method = "releaseUsing(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)Z", at = @At("HEAD"))
    private void graniteskills$chargedShotBowContextHead(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks,
            CallbackInfoReturnable<Boolean> cir) {
        FabricChargedShot.onBowStoppedUsingHead(stack, world, user, remainingUseTicks);
    }

    @Inject(method = "releaseUsing(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)Z", at = @At("RETURN"))
    private void graniteskills$chargedShotBowContextReturn(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks,
            CallbackInfoReturnable<Boolean> cir) {
        FabricChargedShot.onBowStoppedUsingReturn();
    }

    @Inject(method = "shootProjectile(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/projectile/Projectile;IFFFLnet/minecraft/world/entity/LivingEntity;)V", at = @At("TAIL"))
    private void graniteskills$chargedShotAfterShoot(LivingEntity shooter, Projectile projectile, int index, float speed,
            float divergence, float yaw, @Nullable LivingEntity target, CallbackInfo ci) {
        FabricChargedShot.onBowShotProjectile(shooter, projectile);
    }
}
