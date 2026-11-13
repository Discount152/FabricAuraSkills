package dev.granite.graniteskills.fabric.mixin;

import dev.granite.graniteskills.fabric.ability.FabricEnduranceAbilities;
import dev.granite.graniteskills.fabric.ability.FabricHealingAbilities;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityHealMixin {

    @ModifyVariable(method = "heal", at = @At("HEAD"), argsOnly = true)
    private float graniteskills$adjustHeal(float amount) {
        if (!((Object) this instanceof ServerPlayer player)) {
            return amount;
        }
        float adjusted = FabricHealingAbilities.adjustHeal(player, amount);
        return FabricEnduranceAbilities.adjustHeal(player, adjusted);
    }
}
