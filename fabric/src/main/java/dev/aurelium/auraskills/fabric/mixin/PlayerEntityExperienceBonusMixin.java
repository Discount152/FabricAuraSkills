package dev.aurelium.auraskills.fabric.mixin;

import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.FabricExperienceBonus;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Equivalent to Bukkit {@code PlayerExpChangeEvent} handling in {@code ExperienceBonusTrait}:
 * scales the int argument to {@link PlayerEntity#addExperience(int)} on the logical server.
 */
@Mixin(PlayerEntity.class)
public abstract class PlayerEntityExperienceBonusMixin {

    @ModifyVariable(method = "addExperience", at = @At("HEAD"), argsOnly = true)
    private int auraskills$experienceBonus(int experience) {
        if (experience == 0) {
            return 0;
        }
        PlayerEntity self = (PlayerEntity) (Object) this;
        if (self.getEntityWorld().isClient() || !(self instanceof ServerPlayerEntity sp)) {
            return experience;
        }
        return FabricExperienceBonus.scaleForPlayer(FabricAuraSkills.getInstance(), sp, experience);
    }
}
