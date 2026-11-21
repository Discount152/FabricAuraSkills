package dev.granite.graniteskills.fabric.mixin;

import dev.granite.graniteskills.api.ability.Abilities;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import dev.granite.graniteskills.fabric.ability.FabricArcheryAbilities;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.TimeUnit;

/**
 * Fabric port of Bukkit Retrieval ({@code ProjectileHitEvent} on block, delayed pickup).
 */
@Mixin(AbstractArrow.class)
public abstract class AbstractArrowRetrievalMixin {

    @Inject(method = "onHitBlock", at = @At("TAIL"))
    private void graniteskills$retrieval(BlockHitResult hitResult, CallbackInfo ci) {
        AbstractArrow arrow = (AbstractArrow) (Object) this;
        if (arrow.level().isClientSide()) {
            return;
        }
        if (arrow.pickup != AbstractArrow.Pickup.ALLOWED) {
            return;
        }
        Entity owner = arrow.getOwner();
        if (!(owner instanceof ServerPlayer player)) {
            return;
        }
        FabricGraniteSkills plugin = FabricGraniteSkills.getInstance();
        if (plugin == null) {
            return;
        }
        ItemStack arrowItem = arrow.getPickupItemStackOrigin().copy();
        if (arrowItem.isEmpty()) {
            return;
        }
        long delayMs = Math.round(Abilities.RETRIEVAL.optionDouble("delay_sec", 3) * 1000.0);
        plugin.getScheduler().scheduleSync(() -> {
            if (!arrow.isAlive() || arrow.isRemoved()) {
                return;
            }
            if (player.level() != arrow.level()) {
                return;
            }
            FabricArcheryAbilities.applyRetrieval(player, arrow, arrowItem, Double.NaN);
        }, delayMs, TimeUnit.MILLISECONDS);
    }
}
