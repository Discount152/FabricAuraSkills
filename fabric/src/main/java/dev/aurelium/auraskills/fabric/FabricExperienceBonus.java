package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.api.trait.Traits;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.fabric.trait.FabricTraitWorlds;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Bukkit {@code PlayerExpChangeEvent} equivalent: scales the vanilla <strong>point</strong> delta
 * when {@link net.minecraft.entity.player.PlayerEntity#addExperience(int)} runs on the server
 * (same formula as Bukkit {@code ExperienceBonusTrait}).
 */
public final class FabricExperienceBonus {

    private FabricExperienceBonus() {
    }

    /**
     * Pure math: {@code (int) (amount * (1 + effectiveTrait / 100))} — exposed for unit tests.
     */
    public static int applyTraitToPointDelta(int amount, double effectiveTraitLevel) {
        if (amount == 0) {
            return 0;
        }
        double bonus = effectiveTraitLevel / 100.0;
        return (int) (amount * (1.0 + bonus));
    }

    public static int scaleForPlayer(FabricAuraSkills plugin, ServerPlayerEntity player, int experience) {
        if (plugin == null || experience == 0) {
            return experience;
        }
        if (FabricTraitWorlds.isInDisabledWorld(plugin, player)) {
            return experience;
        }
        User user = plugin.getUserManager().getUser(player.getUuid());
        double bonus = user.getEffectiveTraitLevel(Traits.EXPERIENCE_BONUS) / 100.0;
        return (int) (experience * (1.0 + bonus));
    }
}
