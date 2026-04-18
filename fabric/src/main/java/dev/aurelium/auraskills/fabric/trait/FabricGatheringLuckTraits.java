package dev.aurelium.auraskills.fabric.trait;

import dev.aurelium.auraskills.api.ability.Abilities;
import dev.aurelium.auraskills.api.ability.Ability;
import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.skill.Skills;
import dev.aurelium.auraskills.api.trait.Trait;
import dev.aurelium.auraskills.api.trait.Traits;
import dev.aurelium.auraskills.api.trait.TraitModifier;
import dev.aurelium.auraskills.api.util.AuraSkillsModifier.Operation;
import dev.aurelium.auraskills.api.util.NumberUtil;
import dev.aurelium.auraskills.common.source.SourceTag;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.ability.FabricAbilityContext;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * Ports Bukkit {@link dev.aurelium.auraskills.bukkit.trait.GatheringLuckTraits} reload wiring so luck traits gain
 * values from their paired abilities. Extra drops on block interact are handled by
 * {@link dev.aurelium.auraskills.fabric.FabricInteractGatheringLuck}.
 */
public final class FabricGatheringLuckTraits extends FabricTraitImpl {

    private final FabricAbilityContext abilityContext;

    public FabricGatheringLuckTraits(FabricAuraSkills plugin) {
        super(plugin, Traits.FARMING_LUCK, Traits.FORAGING_LUCK, Traits.MINING_LUCK, Traits.EXCAVATION_LUCK,
                Traits.FISHING_LUCK);
        this.abilityContext = new FabricAbilityContext(plugin);
    }

    @Override
    public double getBaseLevel(ServerPlayerEntity player, Trait trait) {
        return 0;
    }

    @Override
    public String getMenuDisplay(double value, Trait trait, Locale locale) {
        return NumberUtil.format1(value);
    }

    @Override
    protected void reload(ServerPlayerEntity player, Trait trait) {
        User user = plugin.getUserManager().getUser(player.getUuid());
        Ability ability = abilityForTrait(trait);
        if (ability == null) {
            return;
        }
        String modifierName = modifierNameFor(ability);
        if (!ability.isEnabled() || abilityContext.isDisabled(ability)) {
            user.removeTraitModifier(modifierName, false);
            return;
        }
        if (abilityContext.failsChecks(player, ability)) {
            user.removeTraitModifier(modifierName, false);
            return;
        }
        double value = ability.getValue(user.getAbilityLevel(ability));
        if (value != 0) {
            TraitModifier modifier = new TraitModifier(modifierName, trait, value, Operation.ADD);
            modifier.setNonPersistent();
            user.addTraitModifier(modifier, false);
        } else {
            user.removeTraitModifier(modifierName, false);
        }
    }

    private static String modifierNameFor(Ability ability) {
        if (ability == Abilities.BOUNTIFUL_HARVEST) {
            return "farming_luck_ability";
        }
        if (ability == Abilities.LUMBERJACK) {
            return "foraging_luck_ability";
        }
        if (ability == Abilities.LUCKY_MINER) {
            return "mining_luck_ability";
        }
        if (ability == Abilities.LUCKY_CATCH) {
            return "fishing_luck_ability";
        }
        if (ability == Abilities.BIGGER_SCOOP) {
            return "excavation_luck_ability";
        }
        return ability.name().toLowerCase(Locale.ROOT) + "_ability";
    }

    private static @Nullable Ability abilityForTrait(Trait trait) {
        if (trait.equals(Traits.FARMING_LUCK)) {
            return Abilities.BOUNTIFUL_HARVEST;
        }
        if (trait.equals(Traits.FORAGING_LUCK)) {
            return Abilities.LUMBERJACK;
        }
        if (trait.equals(Traits.MINING_LUCK)) {
            return Abilities.LUCKY_MINER;
        }
        if (trait.equals(Traits.EXCAVATION_LUCK)) {
            return Abilities.BIGGER_SCOOP;
        }
        if (trait.equals(Traits.FISHING_LUCK)) {
            return Abilities.LUCKY_CATCH;
        }
        return null;
    }

    public static @Nullable Trait traitForSkill(Skill skill) {
        if (skill == Skills.FARMING) {
            return Traits.FARMING_LUCK;
        }
        if (skill == Skills.FORAGING) {
            return Traits.FORAGING_LUCK;
        }
        if (skill == Skills.MINING) {
            return Traits.MINING_LUCK;
        }
        if (skill == Skills.EXCAVATION) {
            return Traits.EXCAVATION_LUCK;
        }
        if (skill == Skills.FISHING) {
            return Traits.FISHING_LUCK;
        }
        return null;
    }

    public static @Nullable SourceTag luckTagForSkill(Skill skill) {
        if (skill == Skills.FARMING) {
            return SourceTag.FARMING_LUCK_APPLICABLE;
        }
        if (skill == Skills.FORAGING) {
            return SourceTag.FORAGING_LUCK_APPLICABLE;
        }
        if (skill == Skills.MINING) {
            return SourceTag.MINING_LUCK_APPLICABLE;
        }
        if (skill == Skills.EXCAVATION) {
            return SourceTag.EXCAVATION_LUCK_APPLICABLE;
        }
        return null;
    }
}
