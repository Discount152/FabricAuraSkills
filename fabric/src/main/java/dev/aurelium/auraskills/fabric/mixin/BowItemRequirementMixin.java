package dev.aurelium.auraskills.fabric.mixin;

import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.requirement.FabricRequirementHandlers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Cancels bow shots when the held bow fails item requirements (Bukkit {@code EntityShootBowEvent}).
 * Crossbows use {@link CrossbowItemRequirementMixin}.
 */
@Mixin(BowItem.class)
public abstract class BowItemRequirementMixin {

    @Inject(method = "shootProjectile(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/projectile/Projectile;IFFFLnet/minecraft/world/entity/LivingEntity;)V",
            at = @At("HEAD"), cancellable = true)
    private void auraskills$cancelBowIfRequirementsFail(LivingEntity shooter, Projectile projectile, int index,
            float speed, float divergence, float yaw, @Nullable LivingEntity target, CallbackInfo ci) {
        if (!(shooter instanceof ServerPlayer sp)) {
            return;
        }
        FabricAuraSkills plugin = FabricAuraSkills.getInstance();
        if (plugin == null || !plugin.isRuntimeReady()) {
            return;
        }
        ItemStack main = sp.getMainHandItem();
        ItemStack off = sp.getOffhandItem();
        ItemStack bow = !main.isEmpty() && main.getItem() instanceof BowItem ? main
                : (!off.isEmpty() && off.getItem() instanceof BowItem ? off : ItemStack.EMPTY);
        if (!bow.isEmpty() && !FabricRequirementHandlers.allowProjectileWeaponShot(plugin, sp, bow)) {
            ci.cancel();
        }
    }
}
