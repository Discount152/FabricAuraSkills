package dev.aurelium.auraskills.fabric.trait;

import dev.aurelium.auraskills.api.trait.Trait;
import dev.aurelium.auraskills.api.trait.Traits;
import dev.aurelium.auraskills.api.util.NumberUtil;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Locale;

/**
 * Fabric counterpart to Bukkit {@code ExperienceBonusTrait} — point scaling is applied in
 * {@code PlayerEntityExperienceBonusMixin} via {@link dev.aurelium.auraskills.fabric.FabricExperienceBonus}.
 */
public final class FabricExperienceBonusTrait extends FabricTraitImpl {

    public FabricExperienceBonusTrait(FabricAuraSkills plugin) {
        super(plugin, Traits.EXPERIENCE_BONUS);
    }

    @Override
    public double getBaseLevel(ServerPlayerEntity player, Trait trait) {
        return 0.0;
    }

    @Override
    public String getMenuDisplay(double value, Trait trait, Locale locale) {
        return "+" + NumberUtil.format1(value) + "%";
    }
}
