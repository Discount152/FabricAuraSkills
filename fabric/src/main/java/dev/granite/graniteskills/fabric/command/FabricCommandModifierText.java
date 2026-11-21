package dev.granite.graniteskills.fabric.command;

import dev.granite.graniteskills.api.stat.Stat;
import dev.granite.graniteskills.api.stat.StatModifier;
import dev.granite.graniteskills.api.trait.Trait;
import dev.granite.graniteskills.api.trait.TraitModifier;
import dev.granite.graniteskills.api.util.GraniteSkillsModifier;
import dev.granite.graniteskills.api.util.NumberUtil;
import dev.granite.graniteskills.common.trait.TraitManager;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import net.minecraft.server.level.ServerPlayer;

import java.util.Locale;

/**
 * Message placeholders for **player** stat/trait modifiers (Bukkit {@code StatFormat} shape;
 * no Bukkit types).
 */
public final class FabricCommandModifierText {

    private FabricCommandModifierText() {}

    public static String applyStatModifier(
            FabricGraniteSkills p,
            String input,
            StatModifier mod,
            ServerPlayer target,
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
            FabricGraniteSkills p,
            String input,
            TraitModifier mod,
            ServerPlayer target,
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

    public static String playerOnly(String input, ServerPlayer target) {
        return input.replace("{player}", target.getName().getString());
    }

    public static String nameAndPlayer(
            String input, String modifierName, ServerPlayer target) {
        return input.replace("{name}", modifierName)
                .replace("{player}", target.getName().getString());
    }

    public static String applyStatHeader(
            FabricGraniteSkills p, String input, Stat stat, ServerPlayer target, Locale lo) {
        return p.getMessageProvider()
                .applyFormatting(
                        input.replace("{color}", stat.getColor(lo))
                                .replace("{symbol}", stat.getSymbol(lo))
                                .replace("{stat}", stat.getDisplayName(lo, false))
                                .replace("{player}", target.getName().getString()));
    }

    public static String applyTraitHeader(
            FabricGraniteSkills p, String input, Trait trait, ServerPlayer target, Locale lo) {
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

    private static String formatValue(GraniteSkillsModifier<?> modifier) {
        return switch (modifier.operation()) {
            case ADD -> (modifier.value() >= 0.0 ? "+" : "")
                    + NumberUtil.format2(modifier.value());
            case ADD_PERCENT -> (modifier.value() >= 0.0 ? "+" : "")
                    + NumberUtil.format2(modifier.value())
                    + "%";
            case MULTIPLY -> NumberUtil.format2(modifier.value()) + "x";
        };
    }

    /** Held item stat/trait list lines (no {player} context). */
    public static String applyStatModifierItem(
            FabricGraniteSkills p, String input, StatModifier mod, Locale lo) {
        Stat stat = mod.stat();
        return p.getMessageProvider()
                .applyFormatting(
                        input.replace("{color}", stat.getColor(lo))
                                .replace("{symbol}", stat.getSymbol(lo))
                                .replace("{stat}", stat.getDisplayName(lo, false))
                                .replace("{value}", formatValue(mod))
                                .replace("{name}", mod.name())
                                .replace("{operation}", mod.operation().toString())
                                .replace("{player}", ""));
    }

    public static String applyTraitModifierItem(
            FabricGraniteSkills p, String input, TraitModifier mod, Locale lo) {
        Trait trait = mod.trait();
        Stat st = firstLinkedStat(p.getTraitManager(), trait);
        return p.getMessageProvider()
                .applyFormatting(
                        input.replace("{color}", st != null ? st.getColor(lo) : "")
                                .replace("{symbol}", "")
                                .replace("{stat}", trait.getDisplayName(lo, false))
                                .replace("{trait}", trait.getDisplayName(lo, false))
                                .replace("{value}", formatValue(mod))
                                .replace("{name}", mod.name())
                                .replace("{player}", ""));
    }
}
