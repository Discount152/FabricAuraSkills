package dev.granite.graniteskills.fabric.mixin;

import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import dev.granite.graniteskills.fabric.ability.FabricAlchemyAbilities;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fabric port of Bukkit Splasher: multiply splash potion duration by
 * {@code 1 + (value/100 * affectedPlayers)} via {@link DataComponents#POTION_DURATION_SCALE}.
 */
@Mixin(ThrownSplashPotion.class)
public abstract class ThrownSplashPotionSplasherMixin {

    @Unique
    private Float graniteskills$previousDurationScale;

    @Inject(method = "onHitAsPotion", at = @At("HEAD"))
    private void graniteskills$splasherScaleHead(ServerLevel level, ItemStack stack, HitResult hitResult, CallbackInfo ci) {
        graniteskills$previousDurationScale = null;
        ThrownSplashPotion self = (ThrownSplashPotion) (Object) this;
        Entity owner = self.getOwner();
        if (!(owner instanceof ServerPlayer shooter)) {
            return;
        }
        FabricGraniteSkills plugin = FabricGraniteSkills.getInstance();
        if (plugin == null) {
            return;
        }
        int affectedPlayers = graniteskills$countAffectedPlayers(self, level, hitResult);
        double multiplier = FabricAlchemyAbilities.getSplasherMultiplier(shooter, affectedPlayers);
        if (multiplier <= 1.0) {
            return;
        }
        float previous = stack.getOrDefault(DataComponents.POTION_DURATION_SCALE, 1.0f);
        graniteskills$previousDurationScale = previous;
        stack.set(DataComponents.POTION_DURATION_SCALE, (float) (previous * multiplier));
    }

    @Inject(method = "onHitAsPotion", at = @At("RETURN"))
    private void graniteskills$splasherScaleReturn(ServerLevel level, ItemStack stack, HitResult hitResult, CallbackInfo ci) {
        if (graniteskills$previousDurationScale == null) {
            return;
        }
        stack.set(DataComponents.POTION_DURATION_SCALE, graniteskills$previousDurationScale);
        graniteskills$previousDurationScale = null;
    }

    @Unique
    private static int graniteskills$countAffectedPlayers(ThrownSplashPotion potion, ServerLevel level, HitResult hitResult) {
        AABB box = potion.getBoundingBox().move(hitResult.getLocation().subtract(potion.position())).inflate(4.0, 2.0, 4.0);
        FabricGraniteSkills plugin = FabricGraniteSkills.getInstance();
        int count = 0;
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (!(entity instanceof ServerPlayer player) || !player.isAffectedByPotions()) {
                continue;
            }
            if (plugin != null && plugin.getUserManager().getUser(player.getUUID()) == null) {
                continue;
            }
            Vec3 delta = hitResult.getLocation().subtract(potion.position());
            AABB entityBox = potion.getBoundingBox().move(delta);
            if (entityBox.inflate(0.3).distanceToSqr(player.getBoundingBox()) < 16.0) {
                count++;
            }
        }
        return count;
    }
}
