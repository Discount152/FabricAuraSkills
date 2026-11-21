package dev.granite.graniteskills.fabric.mixin;

import dev.granite.graniteskills.fabric.ability.FabricHealingAbilities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * {@code HealOrHarmMobEffect} overrides {@code applyInstantaneousEffect}, so the parent
 * {@link MobEffectLifeEssenceMixin} never runs for potion heals.
 */
@Mixin(targets = "net.minecraft.world.effect.HealOrHarmMobEffect")
public abstract class HealOrHarmLifeEssenceMixin {

    @Inject(method = "applyInstantaneousEffect", at = @At("HEAD"))
    private void graniteskills$markMagicHeal(ServerLevel level, Entity source, Entity attacker, LivingEntity target,
            int amplifier, double proximity, CallbackInfo ci) {
        FabricHealingAbilities.markMagicHeal();
    }

    @Inject(method = "applyInstantaneousEffect", at = @At("RETURN"))
    private void graniteskills$clearMagicHeal(ServerLevel level, Entity source, Entity attacker, LivingEntity target,
            int amplifier, double proximity, CallbackInfo ci) {
        FabricHealingAbilities.clearMagicHeal();
    }
}
