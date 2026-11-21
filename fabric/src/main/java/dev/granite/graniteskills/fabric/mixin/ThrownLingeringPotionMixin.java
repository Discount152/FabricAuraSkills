package dev.granite.graniteskills.fabric.mixin;

import dev.granite.graniteskills.fabric.ability.FabricAlchemyAbilities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownLingeringPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Ports Bukkit lingering: after the vanilla lingering cloud is spawned, slow its radius decay.
 */
@Mixin(ThrownLingeringPotion.class)
public abstract class ThrownLingeringPotionMixin {

    @Inject(method = "onHitAsPotion", at = @At("TAIL"))
    private void graniteskills$lingering(ServerLevel level, ItemStack stack, HitResult hit, CallbackInfo ci) {
        ThrownLingeringPotion self = (ThrownLingeringPotion) (Object) this;
        Entity owner = self.getOwner();
        if (!(owner instanceof ServerPlayer player)) {
            return;
        }
        double x = self.getX();
        double y = self.getY();
        double z = self.getZ();
        if (hit instanceof EntityHitResult entityHit) {
            Entity target = entityHit.getEntity();
            x = target.getX();
            y = target.getY();
            z = target.getZ();
        }
        AABB box = new AABB(x - 0.5, y - 0.5, z - 0.5, x + 0.5, y + 0.5, z + 0.5);
        for (AreaEffectCloud cloud : level.getEntitiesOfClass(AreaEffectCloud.class, box)) {
            Entity cloudOwner = cloud.getOwner();
            if (cloudOwner == player || cloud.distanceToSqr(x, y, z) < 1.0) {
                FabricAlchemyAbilities.applyLingering(player, cloud, stack);
                return;
            }
        }
    }
}
