package dev.aurelium.auraskills.fabric.command;

import dev.aurelium.auraskills.api.stat.Stat;
import dev.aurelium.auraskills.api.stat.StatModifier;
import dev.aurelium.auraskills.api.trait.Trait;
import dev.aurelium.auraskills.api.trait.TraitModifier;
import dev.aurelium.auraskills.api.util.AuraSkillsModifier;
import dev.aurelium.auraskills.api.util.NumberUtil;
import dev.aurelium.auraskills.common.trait.TraitManager;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Locale;

/**
 * Message placeholders for **player** stat/trait modifiers (Bukkit {@code StatFormat} shape;
 * no Bukkit types).
 */
public final class FabricCommandModifierText {

    private FabricCommandModifierText() {}

    public static String applyStatModifier(
            FabricAuraSkills p,
            String input,
            StatModifier mod,
            ServerPlayerEntity target,
            Locale lo) {
        Stat stat = mod.stat();
        return p.getMessageProvider()
                .applyFormatting(
                        input.replace("{color}", stat.getColor(lo))
                                .replace("{symbol}", stat.getSymbol(lo))
                                .replace("{stat}", stat.getDisplayName(lo, false))
                                .replace("{value}", formatValue(mod))
                                .replace("{name}", mod.name())
                                .replace("{operation}", mod.operation().toString())
                                .replace(
                                        "{player}",
                                        target.getName().getString()));
    }

    public static String applyTraitModifier(
            FabricAuraSkills p,
            String input,
            TraitModifier mod,
            ServerPlayerEntity target,
            Locale lo) {
        Trait trait = mod.trait();
        Stat stat = firstLinkedStat(p.getTraitManager(), trait);
        return p.getMessageProvider()
                .applyFormatting(
                        input.replace("{color}", stat != null ? stat.getColor(lo) : "")
                                .replace("{symbol}", "")
                                .replace("{stat}", trait.getDisplayName(lo, false))
                                .replace("{trait}", trait.getDisplayName(lo, false))
                                .replace("{value}", formatValue(mod))
                                .replace("{name}", mod.name())
                                .replace("{player}", target.getName().getString()));
    }

    public static String playerOnly(String input, ServerPlayerEntity target) {
        return input.replace("{player}", target.getName().getString());
    }

    public static String nameAndPlayer(
            String input, String modifierName, ServerPlayerEntity target) {
        return input.replace("{name}", modifierName)
                .replace("{player}", target.getName().getString());
    }

    public static String applyStatHeader(
            FabricAuraSkills p, String input, Stat stat, ServerPlayerEntity target, Locale lo) {
        return p.getMessageProvider()
                .applyFormatting(
                        input.replace("{color}", stat.getColor(lo))
                                .replace("{symbol}", stat.getSymbol(lo))
                                .replace("{stat}", stat.getDisplayName(lo, false))
                                .replace("{player}", target.getName().getString()));
    }

    public static String applyTraitHeader(
            FabricAuraSkills p, String input, Trait trait, ServerPlayerEntity target, Locale lo) {
        Stat stat = firstLinkedStat(p.getTraitManager(), trait);
        return p.getMessageProvider()
                .applyFormatting(
                        input.replace("{color}", stat != null ? stat.getColor(lo) : "")
                                .replace("{symbol}", "")
                                .replace("{stat}", trait.getDisplayName(lo, false))
                                .replace("{trait}", trait.getDisplayName(lo, false))
                                .replace("{player}", target.getName().getString()));
    }

    private static Stat firstLinkedStat(TraitManager tm, Trait trait) {
        return tm.getLinkedStats(trait).stream().findFirst().orElse(null);
    }

    private static String formatValue(AuraSkillsModifier<?> modifier) {
        return switch (modifier.operation()) {
            case ADD -> (modifier.value() >= 0.0 ? "+" : "")
                    + NumberUtil.format2(modifier.value());
            case ADD_PERCENT -> (modifier.value() >= 0.0 ? "+" : "")
                    + NumberUtil.format2(modifier.value())
                    + "%";
            case MULTIPLY -> NumberUtil.format2(modifier.value()) + "x";
        };
    }
}
