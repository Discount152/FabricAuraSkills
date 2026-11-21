package dev.granite.graniteskills.fabric.mixin;

import dev.granite.graniteskills.fabric.trait.FabricExperienceBonus;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerExperienceBonusMixin {

    @ModifyVariable(method = "giveExperiencePoints", at = @At("HEAD"), argsOnly = true)
    private int graniteskills$experienceBonus(int amount) {
        return FabricExperienceBonus.scale((ServerPlayer) (Object) this, amount);
    }
}
