package dev.aurelium.auraskills.fabric.loot;

import dev.aurelium.auraskills.api.ability.Abilities;
import dev.aurelium.auraskills.api.loot.LootPool;
import dev.aurelium.auraskills.api.source.XpSource;
import dev.aurelium.auraskills.common.AuraSkillsPlugin;
import dev.aurelium.auraskills.common.loot.AbstractLootHandler;
import dev.aurelium.auraskills.common.loot.SkillLootProvider;
import dev.aurelium.auraskills.common.source.SourceTag;
import dev.aurelium.auraskills.common.user.User;

/**
 * Bukkit {@code ExcavationLootProvider} (Metal Detector / Lucky Spades pools) on Fabric.
 */
public final class FabricExcavationLootProvider extends SkillLootProvider {

    public FabricExcavationLootProvider(AuraSkillsPlugin plugin, AbstractLootHandler handler) {
        super(plugin, handler);
    }

    @Override
    public double getChance(LootPool pool, User user) {
        double chance = handler.getCommonChance(pool, user);
        if ("rare".equals(pool.getName()) && Abilities.METAL_DETECTOR.isEnabled()) {
            chance = handler.getAbilityModifiedChance(chance, Abilities.METAL_DETECTOR, user);
        } else if ("epic".equals(pool.getName()) && Abilities.LUCKY_SPADES.isEnabled()) {
            chance = handler.getAbilityModifiedChance(chance, Abilities.LUCKY_SPADES, user);
        }
        return chance;
    }

    @Override
    public dev.aurelium.auraskills.api.loot.LootDropCause getCause(LootPool pool) {
        if ("rare".equals(pool.getName()) && Abilities.METAL_DETECTOR.isEnabled()) {
            return FabricLootCauses.METAL_DETECTOR;
        } else if ("epic".equals(pool.getName()) && Abilities.LUCKY_SPADES.isEnabled()) {
            return FabricLootCauses.LUCKY_SPADES;
        }
        return FabricLootCauses.EXCAVATION_OTHER_LOOT;
    }

    @Override
    public boolean isApplicable(LootPool pool, XpSource source) {
        if ("rare".equals(pool.getName()) && !plugin.getSkillManager().hasTag(source, SourceTag.METAL_DETECTOR_APPLICABLE)) {
            return false;
        }
        return !"epic".equals(pool.getName()) || plugin.getSkillManager().hasTag(source, SourceTag.LUCKY_SPADES_APPLICABLE);
    }
}
