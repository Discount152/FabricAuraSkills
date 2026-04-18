package dev.aurelium.auraskills.fabric.mixin;

import dev.aurelium.auraskills.fabric.FabricChargedShot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BowItem.class)
public abstract class BowItemChargedShotMixin {

    @Inject(method = "onStoppedUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)Z", at = @At("HEAD"))
    private void auraskills$chargedShotBowContextHead(ItemStack stack, World world, LivingEntity user, int remainingUseTicks,
            CallbackInfoReturnable<Boolean> cir) {
        FabricChargedShot.onBowStoppedUsingHead(stack, world, user, remainingUseTicks);
    }

    @Inject(method = "onStoppedUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)Z", at = @At("RETURN"))
    private void auraskills$chargedShotBowContextReturn(ItemStack stack, World world, LivingEntity user, int remainingUseTicks,
            CallbackInfoReturnable<Boolean> cir) {
        FabricChargedShot.onBowStoppedUsingReturn();
    }

    @Inject(method = "shoot(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/projectile/ProjectileEntity;IFFFLnet/minecraft/entity/LivingEntity;)V", at = @At("TAIL"))
    private void auraskills$chargedShotAfterShoot(LivingEntity shooter, ProjectileEntity projectile, int index, float speed,
            float divergence, float yaw, @Nullable LivingEntity target, CallbackInfo ci) {
        FabricChargedShot.onBowShotProjectile(shooter, projectile);
    }
}
