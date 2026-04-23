package dev.aurelium.auraskills.fabric.trait;

import dev.aurelium.auraskills.api.trait.Trait;
import dev.aurelium.auraskills.api.trait.Traits;
import dev.aurelium.auraskills.api.util.NumberUtil;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Locale;

/**
 * Bukkit {@code DoubleDropTrait} — in-scope drop duplication runs in
 * {@link dev.aurelium.auraskills.fabric.FabricDoubleDrop#register};
 * WorldGuard, {@code LootDropEvent} cancel, and Bukkit’s multi-viewer ordering are N/A.
 */
public final class FabricDoubleDropTrait extends FabricTraitImpl {

    public FabricDoubleDropTrait(FabricAuraSkills plugin) {
        super(plugin, Traits.DOUBLE_DROP);
    }

    @Override
    public double getBaseLevel(ServerPlayerEntity player, Trait trait) {
        return 0.0;
    }

    @Override
    public String getMenuDisplay(double value, Trait trait, Locale locale) {
        return NumberUtil.format1(value) + "%";
    }
}
