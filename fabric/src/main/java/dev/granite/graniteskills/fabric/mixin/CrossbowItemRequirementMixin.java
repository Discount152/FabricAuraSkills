package dev.granite.graniteskills.fabric.mixin;

import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import dev.granite.graniteskills.fabric.requirement.FabricRequirementHandlers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
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

    @Inject(method = "shootProjectile(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/projectile/Projectile;IFFFLnet/minecraft/world/entity/LivingEntity;)V",
            at = @At("HEAD"), cancellable = true)
    private void graniteskills$cancelCrossbowIfRequirementsFail(LivingEntity shooter, Projectile projectile, int index,
            float speed, float divergence, float yaw, @Nullable LivingEntity target, CallbackInfo ci) {
        if (!(shooter instanceof ServerPlayer sp)) {
            return;
        }
        FabricGraniteSkills plugin = FabricGraniteSkills.getInstance();
        if (plugin == null || !plugin.isRuntimeReady()) {
            return;
        }
        ItemStack main = sp.getMainHandItem();
        ItemStack off = sp.getOffhandItem();
        ItemStack crossbow = !main.isEmpty() && main.getItem() instanceof CrossbowItem ? main
                : (!off.isEmpty() && off.getItem() instanceof CrossbowItem ? off : ItemStack.EMPTY);
        if (!crossbow.isEmpty() && !FabricRequirementHandlers.allowProjectileWeaponShot(plugin, sp, crossbow)) {
            ci.cancel();
        }
    }
}
