package dev.aurelium.auraskills.fabric.item;

import dev.aurelium.auraskills.api.item.ModifierType;
import dev.aurelium.auraskills.api.stat.Stat;
import dev.aurelium.auraskills.api.trait.Trait;
import dev.aurelium.auraskills.api.util.AuraSkillsModifier.Operation;
import dev.aurelium.auraskills.api.util.NumberUtil;
import dev.aurelium.auraskills.common.message.type.CommandMessage;
import dev.aurelium.auraskills.common.util.text.TextUtil;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.FabricTraitManager;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.jetbrains.annotations.Nullable;

/**
 * Prepends Bukkit-shaped lore lines for item/armor stat & trait modifier commands.
 */
public final class FabricItemModifierLore {

    private FabricItemModifierLore() {}

    public static void addStatLore(
            FabricAuraSkills plugin,
            ItemStack stack,
            ModifierType type,
            Stat stat,
            double value,
            Operation op,
            Locale lo) {
        CommandMessage key = messageKeyForOp(type, op, value);
        String raw =
                TextUtil.replace(
                        plugin.getMsg(key, lo),
                        "{stat}",
                        stat.getDisplayName(lo, false),
                        "{value}",
                        NumberUtil.format2(Math.abs(value)),
                        "{color}",
                        stat.getColor(lo),
                        "{symbol}",
                        stat.getSymbol(lo));
        String line = plugin.getMessageProvider().applyFormatting(raw);
        prependLoreLine(stack, Text.literal(line));
    }

    public static void addTraitLore(
            FabricAuraSkills plugin,
            ItemStack stack,
            ModifierType type,
            Trait trait,
            double value,
            Operation op,
            Locale lo) {
        CommandMessage key = messageKeyForOp(type, op, value);
        Stat linked =
                plugin.getTraitManager().getLinkedStats(trait).stream()
                        .findFirst()
                        .orElse(null);
        String formatValue;
        FabricTraitManager tm = (FabricTraitManager) plugin.getTraitManager();
        if (op == Operation.ADD
                && !trait.getId().getKey().contains("_luck")
                && tm.getTraitImpl(trait) != null) {
            formatValue = tm.getMenuDisplay(trait, value, lo, null);
        } else {
            formatValue = NumberUtil.format2(Math.abs(value));
        }
        if (formatValue.startsWith("+")) {
            formatValue = formatValue.substring(1);
        }
        String raw =
                TextUtil.replace(
                        plugin.getMsg(key, lo),
                        "{stat}",
                        trait.getDisplayName(lo, false),
                        "{value}",
                        formatValue,
                        "{color}",
                        linked != null ? linked.getColor(lo) : "");
        String line = plugin.getMessageProvider().applyFormatting(raw);
        prependLoreLine(stack, Text.literal(line));
    }

    public static void removeStatLore(
            FabricAuraSkills plugin, ItemStack stack, Stat stat, Locale lo) {
        String needle = stat.getDisplayName(lo, false);
        filterLoreRemoveIfContains(stack, needle);
    }

    private static CommandMessage messageKeyForOp(
            ModifierType type, Operation op, double value) {
        if (type == ModifierType.ITEM) {
            if (op == Operation.ADD_PERCENT) {
                return value >= 0
                        ? CommandMessage.ITEM_MODIFIER_ADD_LORE_ADD_PERCENT
                        : CommandMessage.ITEM_MODIFIER_ADD_LORE_SUBTRACT_PERCENT;
            }
            if (op == Operation.MULTIPLY) {
                return CommandMessage.ITEM_MODIFIER_ADD_LORE_MULTIPLY;
            }
            return value >= 0
                    ? CommandMessage.ITEM_MODIFIER_ADD_LORE
                    : CommandMessage.ITEM_MODIFIER_ADD_LORE_SUBTRACT;
        }
        if (op == Operation.ADD_PERCENT) {
            return value >= 0
                    ? CommandMessage.ARMOR_MODIFIER_ADD_LORE_ADD_PERCENT
                    : CommandMessage.ARMOR_MODIFIER_ADD_LORE_SUBTRACT_PERCENT;
        }
        if (op == Operation.MULTIPLY) {
            return CommandMessage.ARMOR_MODIFIER_ADD_LORE_MULTIPLY;
        }
        return value >= 0
                ? CommandMessage.ARMOR_MODIFIER_ADD_LORE
                : CommandMessage.ARMOR_MODIFIER_ADD_LORE_SUBTRACT;
    }

    private static void prependLoreLine(ItemStack stack, Text line) {
        List<Text> lines = new ArrayList<>();
        lines.add(line);
        LoreComponent existing = stack.get(DataComponentTypes.LORE);
        if (existing != null) {
            lines.addAll(existing.lines());
        }
        stack.set(DataComponentTypes.LORE, new LoreComponent(lines));
    }

    private static void filterLoreRemoveIfContains(
            ItemStack stack, @Nullable String needle) {
        if (needle == null || needle.isEmpty()) {
            return;
        }
        LoreComponent existing = stack.get(DataComponentTypes.LORE);
        if (existing == null) {
            return;
        }
        List<Text> out = new ArrayList<>();
        for (Text t : existing.lines()) {
            String s = t.getString();
            if (!s.contains(needle)) {
                out.add(t);
            }
        }
        if (out.isEmpty()) {
            stack.remove(DataComponentTypes.LORE);
        } else {
            stack.set(DataComponentTypes.LORE, new LoreComponent(out));
        }
    }
}
