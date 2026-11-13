package dev.aurelium.auraskills.fabric.mixin;

import dev.aurelium.auraskills.fabric.ability.FabricEnduranceAbilities;
import dev.aurelium.auraskills.fabric.ability.FabricHealingAbilities;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityHealMixin {

    @ModifyVariable(method = "heal", at = @At("HEAD"), argsOnly = true)
    private float auraskills$adjustHeal(float amount) {
        if (!((Object) this instanceof ServerPlayer player)) {
            return amount;
        }
        float adjusted = FabricHealingAbilities.adjustHeal(player, amount);
        return FabricEnduranceAbilities.adjustHeal(player, adjusted);
    }
}
