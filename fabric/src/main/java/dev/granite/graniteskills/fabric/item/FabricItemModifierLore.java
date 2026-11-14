package dev.granite.graniteskills.fabric.item;

import dev.granite.graniteskills.api.item.ModifierType;
import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.api.stat.Stat;
import dev.granite.graniteskills.api.trait.Trait;
import dev.granite.graniteskills.api.util.GraniteSkillsModifier.Operation;
import dev.granite.graniteskills.api.util.NumberUtil;
import dev.granite.graniteskills.common.message.type.CommandMessage;
import dev.granite.graniteskills.common.util.text.TextUtil;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import dev.granite.graniteskills.fabric.FabricTraitManager;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Prepends Bukkit-shaped lore lines for item/armor stat & trait modifier commands.
 */
public final class FabricItemModifierLore {

    private FabricItemModifierLore() {}

    public static void addStatLore(
            FabricGraniteSkills plugin,
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
        prependLoreLine(stack, Component.literal(line));
    }

    public static void addTraitLore(
            FabricGraniteSkills plugin,
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
        prependLoreLine(stack, Component.literal(line));
    }

    public static void removeStatLore(
            FabricGraniteSkills plugin, ItemStack stack, Stat stat, Locale lo) {
        String needle = stat.getDisplayName(lo, false);
        filterLoreRemoveIfContains(stack, needle);
    }

    /** Bukkit: {@code addRequirementLore} — appends a line (with blank separator if lore exists). */
    public static void addRequirementLore(
            FabricGraniteSkills plugin,
            ItemStack stack,
            ModifierType type,
            Skill skill,
            int level,
            Locale lo) {
        CommandMessage key =
                type == ModifierType.ITEM
                        ? CommandMessage.ITEM_REQUIREMENT_ADD_LORE
                        : CommandMessage.ARMOR_REQUIREMENT_ADD_LORE;
        String raw =
                TextUtil.replace(
                        plugin.getMsg(key, lo),
                        "{skill}",
                        skill.getDisplayName(lo, false),
                        "{level}",
                        String.valueOf(level));
        String line = plugin.getMessageProvider().applyFormatting(raw);
        appendLoreBlock(stack, Component.literal(line));
    }

    /** Bukkit: {@code removeRequirementLore} heuristic — {@code Requires} + capitalized {@link Skill#name()}. */
    public static void removeRequirementLore(Skill skill, ItemStack stack) {
        String cap = TextUtil.capitalize(skill.name().toLowerCase(Locale.ROOT));
        ItemLore existing = stack.get(DataComponents.LORE);
        if (existing == null) {
            return;
        }
        List<Component> out = new ArrayList<>();
        for (Component t : existing.lines()) {
            String s = t.getString();
            if (s.contains("Requires") && s.contains(cap)) {
                continue;
            }
            out.add(t);
        }
        if (out.isEmpty()) {
            stack.remove(DataComponents.LORE);
        } else {
            stack.set(DataComponents.LORE, new ItemLore(out));
        }
    }

    /** Bukkit: {@code addMultiplierLore} — appends. */
    public static void addMultiplierLore(
            FabricGraniteSkills plugin,
            ItemStack stack,
            ModifierType type,
            @Nullable Skill skill,
            double value,
            Locale lo) {
        CommandMessage key;
        if (skill != null) {
            if (value >= 0) {
                key =
                        type == ModifierType.ITEM
                                ? CommandMessage.ITEM_MULTIPLIER_ADD_SKILL_LORE
                                : CommandMessage.ARMOR_MULTIPLIER_ADD_SKILL_LORE;
            } else {
                key =
                        type == ModifierType.ITEM
                                ? CommandMessage.ITEM_MULTIPLIER_ADD_SKILL_LORE_SUBTRACT
                                : CommandMessage.ARMOR_MULTIPLIER_ADD_SKILL_LORE_SUBTRACT;
            }
            String raw =
                    TextUtil.replace(
                            plugin.getMsg(key, lo),
                            "{skill}",
                            skill.getDisplayName(lo, false),
                            "{value}",
                            NumberUtil.format1(Math.abs(value)));
            String line = plugin.getMessageProvider().applyFormatting(raw);
            appendLoreBlock(stack, Component.literal(line));
        } else {
            if (value >= 0) {
                key =
                        type == ModifierType.ITEM
                                ? CommandMessage.ITEM_MULTIPLIER_ADD_GLOBAL_LORE
                                : CommandMessage.ARMOR_MULTIPLIER_ADD_GLOBAL_LORE;
            } else {
                key =
                        type == ModifierType.ITEM
                                ? CommandMessage.ITEM_MULTIPLIER_ADD_GLOBAL_LORE_SUBTRACT
                                : CommandMessage.ARMOR_MULTIPLIER_ADD_GLOBAL_LORE_SUBTRACT;
            }
            String raw =
                    TextUtil.replace(
                            plugin.getMsg(key, lo),
                            "{value}",
                            NumberUtil.format1(Math.abs(value)));
            String line = plugin.getMessageProvider().applyFormatting(raw);
            appendLoreBlock(stack, Component.literal(line));
        }
    }

    private static void appendLoreBlock(ItemStack stack, Component line) {
        List<Component> lines = new ArrayList<>();
        ItemLore existing = stack.get(DataComponents.LORE);
        if (existing != null) {
            lines.addAll(existing.lines());
        }
        if (!lines.isEmpty()) {
            lines.add(Component.literal(" "));
        }
        lines.add(line);
        stack.set(DataComponents.LORE, new ItemLore(lines));
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

    private static void prependLoreLine(ItemStack stack, Component line) {
        List<Component> lines = new ArrayList<>();
        lines.add(line);
        ItemLore existing = stack.get(DataComponents.LORE);
        if (existing != null) {
            lines.addAll(existing.lines());
        }
        stack.set(DataComponents.LORE, new ItemLore(lines));
    }

    private static void filterLoreRemoveIfContains(
            ItemStack stack, @Nullable String needle) {
        if (needle == null || needle.isEmpty()) {
            return;
        }
        ItemLore existing = stack.get(DataComponents.LORE);
        if (existing == null) {
            return;
        }
        List<Component> out = new ArrayList<>();
        for (Component t : existing.lines()) {
            String s = t.getString();
            if (!s.contains(needle)) {
                out.add(t);
            }
        }
        if (out.isEmpty()) {
            stack.remove(DataComponents.LORE);
        } else {
            stack.set(DataComponents.LORE, new ItemLore(out));
        }
    }
}
