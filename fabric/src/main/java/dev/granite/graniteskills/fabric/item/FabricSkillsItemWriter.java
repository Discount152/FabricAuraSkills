package dev.granite.graniteskills.fabric.item;

import dev.granite.graniteskills.api.item.ModifierType;
import dev.granite.graniteskills.api.skill.Multiplier;
import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.api.stat.Stat;
import dev.granite.graniteskills.api.stat.StatModifier;
import dev.granite.graniteskills.api.trait.Trait;
import dev.granite.graniteskills.api.trait.TraitModifier;
import dev.granite.graniteskills.api.util.GraniteSkillsModifier.Operation;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Writes stat/trait item modifier lists into {@code custom_data} / {@code PublicBukkitValues} (Bukkit
 * PDC shape) so {@link FabricSkillsItemReader} matches. When PDC is updated, the legacy {@code
 * GraniteSkills/Modifiers/…} branch for the same {@link ModifierType} is cleared to avoid
 * double-counting.
 */
public final class FabricSkillsItemWriter {

    private static final String PUBLIC_BUKKIT_VALUES = "PublicBukkitValues";
    private static final String PLUGIN_NS = "graniteskills";
    private static final String P_STAT = PLUGIN_NS + ":stat";
    private static final String P_TRAIT = PLUGIN_NS + ":trait";
    private static final String P_VALUE = PLUGIN_NS + ":value";
    private static final String P_OP = PLUGIN_NS + ":operation";

    private FabricSkillsItemWriter() {}

    public static @Nullable CustomData customDataAsComponent(ItemStack stack) {
        return stack.get(DataComponents.CUSTOM_DATA);
    }

    public static @Nullable CompoundTag copyCustomDataRoot(ItemStack stack) {
        CustomData comp = customDataAsComponent(stack);
        if (comp == null) {
            return null;
        }
        return comp.copyTag();
    }

    /**
     * @return false if a stat modifier for {@code stat} is already present (see reader merge rules).
     */
    public static boolean addStatModifier(
            FabricGraniteSkills plugin,
            ItemStack stack,
            ModifierType type,
            boolean offhand,
            Stat stat,
            double value,
            Operation op) {
        List<StatModifier> current =
                new ArrayList<>(
                        FabricSkillsItemReader.getStatModifiers(
                                plugin, stack, type, offhand));
        for (StatModifier m : current) {
            if (m.stat() == stat) {
                return false;
            }
        }
        StatModifier add = new StatModifier("item_cmd", stat, value, op);
        add.setNonPersistent();
        current.add(add);
        putStatList(stack, type, current);
        return true;
    }

    public static boolean removeStatModifier(
            FabricGraniteSkills plugin,
            ItemStack stack,
            ModifierType type,
            boolean offhand,
            Stat stat) {
        List<StatModifier> current =
                new ArrayList<>(
                        FabricSkillsItemReader.getStatModifiers(
                                plugin, stack, type, offhand));
        boolean removed = current.removeIf(m -> m.stat() == stat);
        if (removed) {
            putStatList(stack, type, current);
        }
        return removed;
    }

    public static void removeAllStatModifiers(
            FabricGraniteSkills plugin, ItemStack stack, ModifierType type, boolean offhand) {
        putStatList(stack, type, List.of());
    }

    public static boolean addTraitModifier(
            FabricGraniteSkills plugin,
            ItemStack stack,
            ModifierType type,
            boolean offhand,
            Trait trait,
            double value,
            Operation op) {
        List<TraitModifier> current =
                new ArrayList<>(
                        FabricSkillsItemReader.getTraitModifiers(
                                plugin, stack, type, offhand));
        for (TraitModifier m : current) {
            if (m.trait() == trait) {
                return false;
            }
        }
        TraitModifier add = new TraitModifier("item_cmd_t", trait, value, op);
        add.setNonPersistent();
        current.add(add);
        putTraitList(stack, type, current);
        return true;
    }

    public static boolean removeTraitModifier(
            FabricGraniteSkills plugin,
            ItemStack stack,
            ModifierType type,
            boolean offhand,
            Trait trait) {
        List<TraitModifier> current =
                new ArrayList<>(
                        FabricSkillsItemReader.getTraitModifiers(
                                plugin, stack, type, offhand));
        boolean removed = current.removeIf(m -> m.trait().equals(trait));
        if (removed) {
            putTraitList(stack, type, current);
        }
        return removed;
    }

    public static void removeAllTraitModifiers(
            FabricGraniteSkills plugin, ItemStack stack, ModifierType type, boolean offhand) {
        putTraitList(stack, type, List.of());
    }

    /**
     * Replaces the held-item (or armor) PDC <strong>requirement</strong> compound for
     * {@code PublicBukkitValues} / {@link FabricSkillsItemReader} — clears legacy
     * {@code GraniteSkills/…/Requirements} for the same {@link ModifierType}.
     */
    public static void putRequirements(
            ItemStack stack,
            ModifierType type,
            Map<Skill, Integer> requirements) {
        CompoundTag compound = new CompoundTag();
        for (Map.Entry<Skill, Integer> e : requirements.entrySet()) {
            if (e.getValue() == null || e.getValue() == 0) {
                continue;
            }
            compound.put(requirementNbtKey(e.getKey()), IntTag.valueOf(e.getValue()));
        }
        applyPdcMap(
                stack,
                type,
                FabricSkillsItemReader.SkillsItemMeta.REQUIREMENT,
                compound.size() == 0 ? null : compound);
    }

    /**
     * Replaces the PDC <strong>multiplier</strong> compound (skill id or {@code global}) — clears
     * legacy {@code GraniteSkills/…/Multipliers} for the same {@link ModifierType}.
     */
    public static void putMultipliers(ItemStack stack, ModifierType type, List<Multiplier> multipliers) {
        CompoundTag compound = new CompoundTag();
        for (Multiplier m : multipliers) {
            if (m.value() == 0.0) {
                continue;
            }
            compound.put(multiplierNbtKey(m.skill()), DoubleTag.valueOf(m.value()));
        }
        applyPdcMap(
                stack,
                type,
                FabricSkillsItemReader.SkillsItemMeta.MULTIPLIER,
                compound.size() == 0 ? null : compound);
    }

    private static void putStatList(
            ItemStack stack, ModifierType type, List<StatModifier> modifiers) {
        ListTag list = new ListTag();
        for (StatModifier m : modifiers) {
            CompoundTag c = new CompoundTag();
            c.putString(P_STAT, m.stat().getId().toString());
            c.put(P_VALUE, DoubleTag.valueOf(m.value()));
            c.putString(P_OP, m.operation().name().toLowerCase(Locale.ROOT));
            list.add(c);
        }
        applyPdcList(
                stack, type, FabricSkillsItemReader.SkillsItemMeta.MODIFIER, list);
    }

    private static void putTraitList(
            ItemStack stack, ModifierType type, List<TraitModifier> modifiers) {
        ListTag list = new ListTag();
        for (TraitModifier m : modifiers) {
            CompoundTag c = new CompoundTag();
            c.putString(P_TRAIT, m.trait().getId().toString());
            c.put(P_VALUE, DoubleTag.valueOf(m.value()));
            c.putString(P_OP, m.operation().name().toLowerCase(Locale.ROOT));
            list.add(c);
        }
        applyPdcList(
                stack, type, FabricSkillsItemReader.SkillsItemMeta.TRAIT_MODIFIER, list);
    }

    private static void applyPdcList(
            ItemStack stack,
            ModifierType type,
            FabricSkillsItemReader.SkillsItemMeta meta,
            ListTag list) {
        CompoundTag root = copyOrNewRoot(stack);
        CompoundTag pbv = root.getCompound(PUBLIC_BUKKIT_VALUES).orElseGet(CompoundTag::new);
        String key = pdcKey(type, meta);
        if (list.isEmpty()) {
            pbv.remove(key);
        } else {
            pbv.put(key, list);
        }
        root.put(PUBLIC_BUKKIT_VALUES, pbv);
        stripLegacyInRoot(root, type);
        stripLegacyReqMultInRoot(root, type);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
    }

    private static void applyPdcMap(
            ItemStack stack,
            ModifierType type,
            FabricSkillsItemReader.SkillsItemMeta meta,
            @Nullable CompoundTag mapOrNull) {
        CompoundTag root = copyOrNewRoot(stack);
        CompoundTag pbv = root.getCompound(PUBLIC_BUKKIT_VALUES).orElseGet(CompoundTag::new);
        String key = pdcKey(type, meta);
        if (mapOrNull == null || mapOrNull.size() == 0) {
            pbv.remove(key);
        } else {
            pbv.put(key, mapOrNull);
        }
        root.put(PUBLIC_BUKKIT_VALUES, pbv);
        stripLegacyInRoot(root, type);
        stripLegacyReqMultInRoot(root, type);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
    }

    private static String requirementNbtKey(Skill skill) {
        return PLUGIN_NS + ":" + skill.getId();
    }

    private static String multiplierNbtKey(@Nullable Skill skill) {
        if (skill == null) {
            return PLUGIN_NS + ":global";
        }
        return PLUGIN_NS + ":" + skill.getId();
    }

    private static CompoundTag copyOrNewRoot(ItemStack stack) {
        CustomData comp = stack.get(DataComponents.CUSTOM_DATA);
        if (comp == null) {
            return new CompoundTag();
        }
        return comp.copyTag();
    }

    private static String pdcKey(ModifierType type, FabricSkillsItemReader.SkillsItemMeta m) {
        return PLUGIN_NS
                + ":"
                + type.name().toLowerCase(Locale.ROOT)
                + "_"
                + m.key;
    }

    private static void stripLegacyInRoot(CompoundTag root, ModifierType type) {
        String cap = type == ModifierType.ITEM ? "Item" : "Armor";
        for (String brand : new String[] {"GraniteSkills", "AureliumSkills"}) {
            CompoundTag brandRoot = root.getCompound(brand).orElse(null);
            if (brandRoot == null) {
                continue;
            }
            CompoundTag mods = brandRoot.getCompound("Modifiers").orElse(null);
            if (mods == null) {
                continue;
            }
            if (mods.contains(cap)) {
                mods.remove(cap);
            }
            if (mods.size() == 0) {
                brandRoot.remove("Modifiers");
            }
            if (brandRoot.size() == 0) {
                root.remove(brand);
            }
        }
    }

    private static void stripLegacyReqMultInRoot(CompoundTag root, ModifierType type) {
        String cap = type == ModifierType.ITEM ? "Item" : "Armor";
        for (String brand : new String[] {"GraniteSkills", "AureliumSkills"}) {
            CompoundTag brandRoot = root.getCompound(brand).orElse(null);
            if (brandRoot == null) {
                continue;
            }
            CompoundTag req = brandRoot.getCompound("Requirements").orElse(null);
            if (req != null && req.contains(cap)) {
                req.remove(cap);
                if (req.size() == 0) {
                    brandRoot.remove("Requirements");
                }
            }
            CompoundTag mult = brandRoot.getCompound("Multipliers").orElse(null);
            if (mult != null && mult.contains(cap)) {
                mult.remove(cap);
                if (mult.size() == 0) {
                    brandRoot.remove("Multipliers");
                }
            }
            if (brandRoot.size() == 0) {
                root.remove(brand);
            }
        }
    }
}
