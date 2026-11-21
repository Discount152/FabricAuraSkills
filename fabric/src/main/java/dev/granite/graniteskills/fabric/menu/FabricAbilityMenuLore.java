package dev.granite.graniteskills.fabric.menu;

import dev.granite.graniteskills.api.ability.Ability;
import dev.granite.graniteskills.api.mana.ManaAbilities;
import dev.granite.graniteskills.api.mana.ManaAbility;
import dev.granite.graniteskills.api.util.NumberUtil;
import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import dev.granite.graniteskills.common.ability.AbilityManager;
import dev.granite.graniteskills.common.ability.AbilityUtil;
import dev.granite.graniteskills.common.mana.ManaAbilityManager;
import dev.granite.graniteskills.common.message.type.LevelerFormat;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.common.util.text.TextUtil;

/**
 * Shared passive + mana ability description bodies for Fabric abilities menus, aligned with
 * Bukkit AbilitiesMenu placeholder rules.
 */
public final class FabricAbilityMenuLore {

    public enum PassiveBodyMode {
        LOCKED,
        UPGRADING,
        MAXED
    }

    public enum ManaBodyMode {
        LOCKED,
        UPGRADING,
        MAXED
    }

    private FabricAbilityMenuLore() {}

    public static String passiveAbilityDescriptionBodyPlain(
            GraniteSkillsPlugin plugin,
            AbilityManager am,
            User user,
            Ability ability,
            PassiveBodyMode mode) {
        String desc = am.getBaseDescription(ability, user, false);
        var lo = user.getLocale();
        String format = plugin.getMessageProvider().getRaw(LevelerFormat.DESC_UPGRADE_VALUE, lo);
        switch (mode) {
            case LOCKED ->
                    desc =
                            TextUtil.replace(
                                    desc,
                                    "{value}",
                                    NumberUtil.format1(ability.getValue(1)),
                                    "{value_2}",
                                    NumberUtil.format1(ability.getSecondaryValue(1)),
                                    "{chance_value}",
                                    am.getChanceValue(ability, 1),
                                    "{guaranteed_value}",
                                    am.getGuaranteedValue(ability, 1));
            case UPGRADING -> {
                int lvl = user.getAbilityLevel(ability);
                desc =
                        TextUtil.replace(
                                desc,
                                "{value}",
                                AbilityUtil.getUpgradeValue(ability, lvl, format),
                                "{value_2}",
                                AbilityUtil.getUpgradeValue2(ability, lvl, format),
                                "{chance_value}",
                                am.getChanceValue(ability, lvl),
                                "{guaranteed_value}",
                                am.getGuaranteedValue(ability, lvl));
            }
            case MAXED -> {
                int lvl = user.getAbilityLevel(ability);
                desc =
                        TextUtil.replace(
                                desc,
                                "{value}",
                                NumberUtil.format1(ability.getValue(lvl)),
                                "{value_2}",
                                NumberUtil.format1(ability.getSecondaryValue(lvl)));
            }
        }
        return FabricAbilityLoreNormalization.normalizeExpandedDescription(desc);
    }

    public static String manaAbilityDescriptionBodyPlain(
            GraniteSkillsPlugin plugin,
            ManaAbilityManager mm,
            User user,
            ManaAbility mana,
            ManaBodyMode mode) {
        String desc = mm.getBaseDescription(mana, user, false);
        var lo = user.getLocale();
        String format = plugin.getMessageProvider().getRaw(LevelerFormat.DESC_UPGRADE_VALUE, lo);
        switch (mode) {
            case LOCKED ->
                    desc =
                            TextUtil.replace(
                                    desc,
                                    "{value}",
                                    NumberUtil.format1(mana.getDisplayValue(1)),
                                    "{duration}",
                                    NumberUtil.format1(lockedPreviewDuration(mana)));
            case UPGRADING -> {
                int lvl = user.getManaAbilityLevel(mana);
                desc =
                        TextUtil.replace(
                                desc,
                                "{value}",
                                AbilityUtil.getUpgradeValue(mana, lvl, format),
                                "{duration}",
                                AbilityUtil.getUpgradeDuration(mana, lvl, format));
            }
            case MAXED -> {
                int lvl = user.getManaAbilityLevel(mana);
                desc =
                        TextUtil.replace(
                                desc,
                                "{value}",
                                NumberUtil.format1(mana.getDisplayValue(lvl)),
                                "{duration}",
                                NumberUtil.format1(AbilityUtil.getDuration(mana, lvl)));
            }
        }
        return FabricAbilityLoreNormalization.normalizeExpandedDescription(desc);
    }

    private static double lockedPreviewDuration(ManaAbility mana) {
        if (mana == ManaAbilities.LIGHTNING_BLADE) {
            return ManaAbilities.LIGHTNING_BLADE.optionDouble("base_duration", 5.0);
        }
        return mana.getValue(1);
    }
}
