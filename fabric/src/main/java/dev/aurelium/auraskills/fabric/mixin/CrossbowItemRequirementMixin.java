package dev.aurelium.auraskills.fabric.mixin;

import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.requirement.FabricRequirementHandlers;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Cancels crossbow shots when the held crossbow fails item requirements (Bukkit {@code EntityShootBowEvent} for crossbows).
 */
@Mixin(CrossbowItem.class)
public abstract class CrossbowItemRequirementMixin {

    @Inject(method = "shoot(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/projectile/ProjectileEntity;IFFFLnet/minecraft/entity/LivingEntity;)V",
            at = @At("HEAD"), cancellable = true)
    private void auraskills$cancelCrossbowIfRequirementsFail(LivingEntity shooter, ProjectileEntity projectile, int index,
            float speed, float divergence, float yaw, @Nullable LivingEntity target, CallbackInfo ci) {
        if (!(shooter instanceof ServerPlayerEntity sp)) {
            return;
        }
        FabricAuraSkills plugin = FabricAuraSkills.getInstance();
        if (plugin == null || !plugin.isRuntimeReady()) {
            return;
        }
        ItemStack main = sp.getMainHandStack();
        ItemStack off = sp.getOffHandStack();
        ItemStack crossbow = !main.isEmpty() && main.getItem() instanceof CrossbowItem ? main
                : (!off.isEmpty() && off.getItem() instanceof CrossbowItem ? off : ItemStack.EMPTY);
        if (!crossbow.isEmpty() && !FabricRequirementHandlers.allowProjectileWeaponShot(plugin, sp, crossbow)) {
            ci.cancel();
        }
    }
}
