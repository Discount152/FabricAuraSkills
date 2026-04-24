package dev.aurelium.auraskills.fabric.item;

import dev.aurelium.auraskills.api.item.ModifierType;
import dev.aurelium.auraskills.api.skill.Multiplier;
import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.stat.Stat;
import dev.aurelium.auraskills.api.stat.StatModifier;
import dev.aurelium.auraskills.api.trait.Trait;
import dev.aurelium.auraskills.api.trait.TraitModifier;
import dev.aurelium.auraskills.api.util.AuraSkillsModifier.Operation;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtList;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Writes stat/trait item modifier lists into {@code custom_data} / {@code PublicBukkitValues} (Bukkit
 * PDC shape) so {@link FabricSkillsItemReader} matches. When PDC is updated, the legacy {@code
 * AuraSkills/Modifiers/…} branch for the same {@link ModifierType} is cleared to avoid
 * double-counting.
 */
public final class FabricSkillsItemWriter {

    private static final String PUBLIC_BUKKIT_VALUES = "PublicBukkitValues";
    private static final String PLUGIN_NS = "auraskills";
    private static final String P_STAT = PLUGIN_NS + ":stat";
    private static final String P_TRAIT = PLUGIN_NS + ":trait";
    private static final String P_VALUE = PLUGIN_NS + ":value";
    private static final String P_OP = PLUGIN_NS + ":operation";

    private FabricSkillsItemWriter() {}

    public static @Nullable NbtComponent customDataAsComponent(ItemStack stack) {
        return stack.get(DataComponentTypes.CUSTOM_DATA);
    }

    public static @Nullable NbtCompound copyCustomDataRoot(ItemStack stack) {
        NbtComponent comp = customDataAsComponent(stack);
        if (comp == null) {
            return null;
        }
        return comp.copyNbt();
    }

    /**
     * @return false if a stat modifier for {@code stat} is already present (see reader merge rules).
     */
    public static boolean addStatModifier(
            FabricAuraSkills plugin,
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
            FabricAuraSkills plugin,
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
            FabricAuraSkills plugin, ItemStack stack, ModifierType type, boolean offhand) {
        putStatList(stack, type, List.of());
    }

    public static boolean addTraitModifier(
            FabricAuraSkills plugin,
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
            FabricAuraSkills plugin,
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
            FabricAuraSkills plugin, ItemStack stack, ModifierType type, boolean offhand) {
        putTraitList(stack, type, List.of());
    }

    /**
     * Replaces the held-item (or armor) PDC <strong>requirement</strong> compound for
     * {@code PublicBukkitValues} / {@link FabricSkillsItemReader} — clears legacy
     * {@code AuraSkills/…/Requirements} for the same {@link ModifierType}.
     */
    public static void putRequirements(
            ItemStack stack,
            ModifierType type,
            Map<Skill, Integer> requirements) {
        NbtCompound compound = new NbtCompound();
        for (Map.Entry<Skill, Integer> e : requirements.entrySet()) {
            if (e.getValue() == null || e.getValue() == 0) {
                continue;
            }
            compound.put(requirementNbtKey(e.getKey()), NbtInt.of(e.getValue()));
        }
        applyPdcMap(
                stack,
                type,
                FabricSkillsItemReader.SkillsItemMeta.REQUIREMENT,
                compound.getSize() == 0 ? null : compound);
    }

    /**
     * Replaces the PDC <strong>multiplier</strong> compound (skill id or {@code global}) — clears
     * legacy {@code AuraSkills/…/Multipliers} for the same {@link ModifierType}.
     */
    public static void putMultipliers(ItemStack stack, ModifierType type, List<Multiplier> multipliers) {
        NbtCompound compound = new NbtCompound();
        for (Multiplier m : multipliers) {
            if (m.value() == 0.0) {
                continue;
            }
            compound.put(multiplierNbtKey(m.skill()), NbtDouble.of(m.value()));
        }
        applyPdcMap(
                stack,
                type,
                FabricSkillsItemReader.SkillsItemMeta.MULTIPLIER,
                compound.getSize() == 0 ? null : compound);
    }

    private static void putStatList(
            ItemStack stack, ModifierType type, List<StatModifier> modifiers) {
        NbtList list = new NbtList();
        for (StatModifier m : modifiers) {
            NbtCompound c = new NbtCompound();
            c.putString(P_STAT, m.stat().getId().toString());
            c.put(P_VALUE, NbtDouble.of(m.value()));
            c.putString(P_OP, m.operation().name().toLowerCase(Locale.ROOT));
            list.add(c);
        }
        applyPdcList(
                stack, type, FabricSkillsItemReader.SkillsItemMeta.MODIFIER, list);
    }

    private static void putTraitList(
            ItemStack stack, ModifierType type, List<TraitModifier> modifiers) {
        NbtList list = new NbtList();
        for (TraitModifier m : modifiers) {
            NbtCompound c = new NbtCompound();
            c.putString(P_TRAIT, m.trait().getId().toString());
            c.put(P_VALUE, NbtDouble.of(m.value()));
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
            NbtList list) {
        NbtCompound root = copyOrNewRoot(stack);
        NbtCompound pbv = root.getCompound(PUBLIC_BUKKIT_VALUES).orElseGet(NbtCompound::new);
        String key = pdcKey(type, meta);
        if (list.isEmpty()) {
            pbv.remove(key);
        } else {
            pbv.put(key, list);
        }
        root.put(PUBLIC_BUKKIT_VALUES, pbv);
        stripLegacyInRoot(root, type);
        stripLegacyReqMultInRoot(root, type);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(root));
    }

    private static void applyPdcMap(
            ItemStack stack,
            ModifierType type,
            FabricSkillsItemReader.SkillsItemMeta meta,
            @Nullable NbtCompound mapOrNull) {
        NbtCompound root = copyOrNewRoot(stack);
        NbtCompound pbv = root.getCompound(PUBLIC_BUKKIT_VALUES).orElseGet(NbtCompound::new);
        String key = pdcKey(type, meta);
        if (mapOrNull == null || mapOrNull.getSize() == 0) {
            pbv.remove(key);
        } else {
            pbv.put(key, mapOrNull);
        }
        root.put(PUBLIC_BUKKIT_VALUES, pbv);
        stripLegacyInRoot(root, type);
        stripLegacyReqMultInRoot(root, type);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(root));
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

    private static NbtCompound copyOrNewRoot(ItemStack stack) {
        NbtComponent comp = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (comp == null) {
            return new NbtCompound();
        }
        return comp.copyNbt();
    }

    private static String pdcKey(ModifierType type, FabricSkillsItemReader.SkillsItemMeta m) {
        return PLUGIN_NS
                + ":"
                + type.name().toLowerCase(Locale.ROOT)
                + "_"
                + m.key;
    }

    private static void stripLegacyInRoot(NbtCompound root, ModifierType type) {
        String cap = type == ModifierType.ITEM ? "Item" : "Armor";
        for (String brand : new String[] {"AuraSkills", "AureliumSkills"}) {
            NbtCompound brandRoot = root.getCompound(brand).orElse(null);
            if (brandRoot == null) {
                continue;
            }
            NbtCompound mods = brandRoot.getCompound("Modifiers").orElse(null);
            if (mods == null) {
                continue;
            }
            if (mods.contains(cap)) {
                mods.remove(cap);
            }
            if (mods.getSize() == 0) {
                brandRoot.remove("Modifiers");
            }
            if (brandRoot.getSize() == 0) {
                root.remove(brand);
            }
        }
    }

    private static void stripLegacyReqMultInRoot(NbtCompound root, ModifierType type) {
        String cap = type == ModifierType.ITEM ? "Item" : "Armor";
        for (String brand : new String[] {"AuraSkills", "AureliumSkills"}) {
            NbtCompound brandRoot = root.getCompound(brand).orElse(null);
            if (brandRoot == null) {
                continue;
            }
            NbtCompound req = brandRoot.getCompound("Requirements").orElse(null);
            if (req != null && req.contains(cap)) {
                req.remove(cap);
                if (req.getSize() == 0) {
                    brandRoot.remove("Requirements");
                }
            }
            NbtCompound mult = brandRoot.getCompound("Multipliers").orElse(null);
            if (mult != null && mult.contains(cap)) {
                mult.remove(cap);
                if (mult.getSize() == 0) {
                    brandRoot.remove("Multipliers");
                }
            }
            if (brandRoot.getSize() == 0) {
                root.remove(brand);
            }
        }
    }
}
