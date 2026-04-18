package dev.aurelium.auraskills.fabric.item;

import dev.aurelium.auraskills.api.item.ModifierType;
import dev.aurelium.auraskills.api.registry.NamespacedId;
import dev.aurelium.auraskills.api.skill.Multiplier;
import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.stat.ReloadableIdentifier;
import dev.aurelium.auraskills.api.stat.Stat;
import dev.aurelium.auraskills.api.stat.StatModifier;
import dev.aurelium.auraskills.api.trait.Trait;
import dev.aurelium.auraskills.api.trait.TraitModifier;
import dev.aurelium.auraskills.api.util.AuraSkillsModifier.Operation;
import dev.aurelium.auraskills.common.config.Option;
import dev.aurelium.auraskills.common.util.text.TextUtil;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.FabricUser;
import dev.aurelium.auraskills.fabric.requirement.FabricGlobalRequirement;
import dev.aurelium.auraskills.fabric.requirement.FabricRequirementManager;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Reads item stat/trait/multiplier/requirement data from {@link DataComponentTypes#CUSTOM_DATA} (Paper PDC /
 * {@code PublicBukkitValues}) and legacy {@code AuraSkills}/{@code AureliumSkills} NBT trees.
 */
public final class FabricSkillsItemReader {

    private static final String PUBLIC_BUKKIT_VALUES = "PublicBukkitValues";
    private static final String PLUGIN_NS = "auraskills";

    private FabricSkillsItemReader() {
    }

    private static @Nullable NbtCompound customDataRoot(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        NbtComponent comp = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (comp == null) {
            return null;
        }
        return comp.copyNbt();
    }

    private static @Nullable NbtCompound publicBukkitValues(ItemStack stack) {
        NbtCompound root = customDataRoot(stack);
        if (root == null || !root.contains(PUBLIC_BUKKIT_VALUES)) {
            return null;
        }
        return root.getCompound(PUBLIC_BUKKIT_VALUES).orElse(null);
    }

    private static String pdcContainerKey(ModifierType type, SkillsItemMeta meta) {
        return PLUGIN_NS + ":" + type.name().toLowerCase(Locale.ROOT) + "_" + meta.key;
    }

    public enum SkillsItemMeta {
        MODIFIER("modifiers"),
        TRAIT_MODIFIER("trait_modifiers"),
        REQUIREMENT("requirements"),
        MULTIPLIER("multipliers");

        final String key;

        SkillsItemMeta(String key) {
            this.key = key;
        }
    }

    public static List<StatModifier> getStatModifiers(FabricAuraSkills plugin, ItemStack stack, ModifierType type, boolean offhand) {
        List<StatModifier> out = new ArrayList<>();
        out.addAll(readStatModifiersPdc(plugin, stack, type, offhand));
        if (out.isEmpty()) {
            out.addAll(readStatModifiersLegacy(plugin, stack, type, offhand));
        }
        return out;
    }

    public static List<TraitModifier> getTraitModifiers(FabricAuraSkills plugin, ItemStack stack, ModifierType type, boolean offhand) {
        List<TraitModifier> out = new ArrayList<>();
        out.addAll(readTraitModifiersPdc(plugin, stack, type, offhand));
        return out;
    }

    public static List<Multiplier> getMultipliers(FabricAuraSkills plugin, ItemStack stack, ModifierType type, boolean offhand) {
        List<Multiplier> out = new ArrayList<>();
        out.addAll(readMultipliersPdc(plugin, stack, type, offhand));
        if (out.isEmpty()) {
            out.addAll(readMultipliersLegacy(plugin, stack, type, offhand));
        }
        return out;
    }

    public static Map<Skill, Integer> getRequirements(FabricAuraSkills plugin, ItemStack stack, ModifierType type) {
        Map<Skill, Integer> out = new ConcurrentHashMap<>();
        out.putAll(readRequirementsPdc(plugin, stack, type));
        if (out.isEmpty()) {
            out.putAll(readRequirementsLegacy(plugin, stack, type));
        }
        return out;
    }

    public static boolean meetsRequirements(FabricAuraSkills plugin, ServerPlayerEntity player, ItemStack stack, ModifierType type) {
        if (!plugin.configBoolean(Option.REQUIREMENT_ENABLED)) {
            return true;
        }
        User u = plugin.getUserManager().getUser(player.getUuid());
        if (!(u instanceof FabricUser user)) {
            return true;
        }
        Map<Skill, Integer> itemReq = getRequirements(plugin, stack, type);
        if (!plugin.configBoolean(Option.REQUIREMENT_OVERRIDE_GLOBAL) || itemReq.isEmpty()) {
            FabricRequirementManager rm = plugin.getFabricRequirementManager();
            if (rm != null) {
                Identifier heldId = Registries.ITEM.getId(stack.getItem());
                for (FabricGlobalRequirement global : rm.getGlobalRequirementsType(type)) {
                    if (!global.itemId().equals(heldId)) {
                        continue;
                    }
                    for (Map.Entry<Skill, Integer> entry : global.requirements().entrySet()) {
                        if (user.getSkillLevel(entry.getKey()) < entry.getValue()) {
                            return false;
                        }
                    }
                }
            }
        }
        for (Map.Entry<Skill, Integer> e : itemReq.entrySet()) {
            if (user.getSkillLevel(e.getKey()) < e.getValue()) {
                return false;
            }
        }
        return true;
    }

    private static List<StatModifier> readStatModifiersPdc(FabricAuraSkills plugin, ItemStack stack, ModifierType type, boolean offhand) {
        NbtCompound pbv = publicBukkitValues(stack);
        if (pbv == null) {
            return List.of();
        }
        String key = pdcContainerKey(type, SkillsItemMeta.MODIFIER);
        if (!pbv.contains(key)) {
            return List.of();
        }
        return parseStatContainer(plugin, stack, pbv.get(key), type, offhand);
    }

    private static List<StatModifier> parseStatContainer(FabricAuraSkills plugin, ItemStack stack, NbtElement raw, ModifierType type, boolean offhand) {
        List<StatModifier> list = new ArrayList<>();
        if (raw instanceof NbtCompound compound) {
            for (String k : compound.getKeys()) {
                NbtElement el = compound.get(k);
                if (!(el instanceof NbtDouble d)) {
                    continue;
                }
                if (d.doubleValue() == 0.0) {
                    continue;
                }
                Stat stat = resolveStat(plugin, k);
                if (stat == null) {
                    continue;
                }
                StatModifier mod = new StatModifier(modifierName(stat, type, offhand, stack), stat, d.doubleValue(), Operation.ADD);
                mod.setNonPersistent();
                list.add(mod);
            }
        } else if (raw instanceof NbtList nbtList) {
            for (NbtElement el : nbtList) {
                if (!(el instanceof NbtCompound c)) {
                    continue;
                }
                String statName = firstString(c, PLUGIN_NS + ":stat", "stat");
                if (statName == null) {
                    continue;
                }
                Stat stat = plugin.getStatRegistry().getOrNull(parseId(statName));
                if (stat == null) {
                    continue;
                }
                double value = firstDouble(c, PLUGIN_NS + ":value", "value");
                if (value == 0.0) {
                    continue;
                }
                String opName = firstString(c, PLUGIN_NS + ":operation", "operation");
                Operation op = Operation.parse(opName);
                StatModifier mod = new StatModifier(modifierName(stat, type, offhand, stack), stat, value, op);
                mod.setNonPersistent();
                list.add(mod);
            }
        }
        return list;
    }

    private static List<StatModifier> readStatModifiersLegacy(FabricAuraSkills plugin, ItemStack stack, ModifierType type, boolean offhand) {
        List<StatModifier> list = new ArrayList<>();
        for (String rootName : new String[]{"AuraSkills", "AureliumSkills"}) {
            NbtCompound typeCompound = legacyModifiersTypeCompound(customDataRoot(stack), rootName, type);
            if (typeCompound == null) {
                continue;
            }
            for (String k : typeCompound.getKeys()) {
                NbtElement el = typeCompound.get(k);
                if (!(el instanceof NbtDouble d) && !(el instanceof NbtInt)) {
                    continue;
                }
                double val = el instanceof NbtDouble dd ? dd.doubleValue() : ((NbtInt) el).intValue();
                if (val == 0.0) {
                    continue;
                }
                Stat stat = plugin.getStatRegistry().getOrNull(NamespacedId.fromDefault(k.toLowerCase(Locale.ROOT)));
                if (stat == null) {
                    continue;
                }
                StatModifier mod = new StatModifier(modifierName(stat, type, offhand, stack), stat, val, Operation.ADD);
                mod.setNonPersistent();
                list.add(mod);
            }
        }
        return list;
    }

    private static @Nullable NbtCompound legacyModifiersTypeCompound(@Nullable NbtCompound customRoot, String rootName, ModifierType type) {
        if (customRoot == null) {
            return null;
        }
        NbtCompound root = customRoot.getCompound(rootName).orElse(null);
        if (root == null) {
            return null;
        }
        NbtCompound mods = root.getCompound("Modifiers").orElse(null);
        if (mods == null) {
            return null;
        }
        String cap = TextUtil.capitalize(type.name().toLowerCase(Locale.ROOT));
        return mods.getCompound(cap).orElse(null);
    }

    private static List<TraitModifier> readTraitModifiersPdc(FabricAuraSkills plugin, ItemStack stack, ModifierType type, boolean offhand) {
        NbtCompound pbv = publicBukkitValues(stack);
        if (pbv == null) {
            return List.of();
        }
        String key = pdcContainerKey(type, SkillsItemMeta.TRAIT_MODIFIER);
        if (!pbv.contains(key)) {
            return List.of();
        }
        return parseTraitContainer(plugin, stack, pbv.get(key), type, offhand);
    }

    private static List<TraitModifier> parseTraitContainer(FabricAuraSkills plugin, ItemStack stack, NbtElement raw, ModifierType type, boolean offhand) {
        List<TraitModifier> list = new ArrayList<>();
        if (raw instanceof NbtCompound compound) {
            for (String k : compound.getKeys()) {
                NbtElement el = compound.get(k);
                if (!(el instanceof NbtDouble d)) {
                    continue;
                }
                if (d.doubleValue() == 0.0) {
                    continue;
                }
                Trait trait = resolveTrait(plugin, k);
                if (trait == null) {
                    continue;
                }
                TraitModifier mod = new TraitModifier(traitModifierName(trait, type, offhand, stack), trait, d.doubleValue(), Operation.ADD);
                mod.setNonPersistent();
                list.add(mod);
            }
        } else if (raw instanceof NbtList nbtList) {
            for (NbtElement el : nbtList) {
                if (!(el instanceof NbtCompound c)) {
                    continue;
                }
                String traitName = firstString(c, PLUGIN_NS + ":trait", "trait");
                if (traitName == null) {
                    continue;
                }
                Trait trait = plugin.getTraitRegistry().getOrNull(parseId(traitName));
                if (trait == null) {
                    continue;
                }
                double value = firstDouble(c, PLUGIN_NS + ":value", "value");
                if (value == 0.0) {
                    continue;
                }
                String opName = firstString(c, PLUGIN_NS + ":operation", "operation");
                Operation op = Operation.parse(opName);
                TraitModifier mod = new TraitModifier(traitModifierName(trait, type, offhand, stack), trait, value, op);
                mod.setNonPersistent();
                list.add(mod);
            }
        }
        return list;
    }

    private static List<Multiplier> readMultipliersPdc(FabricAuraSkills plugin, ItemStack stack, ModifierType type, boolean offhand) {
        NbtCompound pbv = publicBukkitValues(stack);
        if (pbv == null) {
            return List.of();
        }
        String key = pdcContainerKey(type, SkillsItemMeta.MULTIPLIER);
        if (!pbv.contains(key)) {
            return List.of();
        }
        NbtElement raw = pbv.get(key);
        if (!(raw instanceof NbtCompound compound)) {
            return List.of();
        }
        List<Multiplier> list = new ArrayList<>();
        for (String k : compound.getKeys()) {
            NbtElement el = compound.get(k);
            if (!(el instanceof NbtDouble d)) {
                continue;
            }
            if (d.doubleValue() == 0.0) {
                continue;
            }
            Skill skill = "global".equalsIgnoreCase(stripNamespace(k))
                    ? null
                    : plugin.getSkillRegistry().getOrNull(NamespacedId.fromDefault(stripNamespace(k)));
            String name = multiplierName(skill, type, offhand, stack);
            list.add(new Multiplier(name, skill, d.doubleValue()));
        }
        return list;
    }

    private static List<Multiplier> readMultipliersLegacy(FabricAuraSkills plugin, ItemStack stack, ModifierType type, boolean offhand) {
        List<Multiplier> list = new ArrayList<>();
        for (String rootName : new String[]{"AuraSkills", "AureliumSkills"}) {
            NbtCompound root = customDataRoot(stack);
            NbtCompound skillsRoot = root == null ? null : root.getCompound(rootName).orElse(null);
            if (skillsRoot == null) {
                continue;
            }
            NbtCompound mult = skillsRoot.getCompound("Multipliers").orElse(null);
            if (mult == null) {
                continue;
            }
            String cap = TextUtil.capitalize(type.name().toLowerCase(Locale.ROOT));
            NbtCompound typeCompound = mult.getCompound(cap).orElse(null);
            if (typeCompound == null) {
                continue;
            }
            for (String k : typeCompound.getKeys()) {
                NbtElement el = typeCompound.get(k);
                double val = el instanceof NbtDouble d ? d.doubleValue() : el instanceof NbtInt i ? i.intValue() : 0.0;
                if (val == 0.0) {
                    continue;
                }
                Skill skill = plugin.getSkillRegistry().getOrNull(NamespacedId.fromDefault(k.toLowerCase(Locale.ROOT)));
                list.add(new Multiplier(multiplierName(skill, type, offhand, stack), skill, val));
            }
        }
        return list;
    }

    private static Map<Skill, Integer> readRequirementsPdc(FabricAuraSkills plugin, ItemStack stack, ModifierType type) {
        Map<Skill, Integer> map = new ConcurrentHashMap<>();
        NbtCompound pbv = publicBukkitValues(stack);
        if (pbv == null) {
            return map;
        }
        String key = pdcContainerKey(type, SkillsItemMeta.REQUIREMENT);
        if (!pbv.contains(key)) {
            return map;
        }
        NbtElement raw = pbv.get(key);
        if (!(raw instanceof NbtCompound compound)) {
            return map;
        }
        for (String k : compound.getKeys()) {
            NbtElement el = compound.get(k);
            int level = el instanceof NbtInt i ? i.intValue() : el instanceof NbtDouble d ? (int) d.doubleValue() : 0;
            if (level == 0) {
                continue;
            }
            Skill skill = plugin.getSkillRegistry().getOrNull(NamespacedId.fromDefault(stripNamespace(k)));
            if (skill != null) {
                map.put(skill, level);
            }
        }
        return map;
    }

    private static Map<Skill, Integer> readRequirementsLegacy(FabricAuraSkills plugin, ItemStack stack, ModifierType type) {
        Map<Skill, Integer> map = new ConcurrentHashMap<>();
        for (String rootName : new String[]{"AuraSkills", "AureliumSkills"}) {
            NbtCompound root = customDataRoot(stack);
            NbtCompound skillsRoot = root == null ? null : root.getCompound(rootName).orElse(null);
            if (skillsRoot == null) {
                continue;
            }
            NbtCompound req = skillsRoot.getCompound("Requirements").orElse(null);
            if (req == null) {
                continue;
            }
            String cap = TextUtil.capitalize(type.name().toLowerCase(Locale.ROOT));
            NbtCompound typeCompound = req.getCompound(cap).orElse(null);
            if (typeCompound == null) {
                continue;
            }
            for (String k : typeCompound.getKeys()) {
                NbtElement el = typeCompound.get(k);
                int level = el instanceof NbtInt i ? i.intValue() : el instanceof NbtDouble d ? (int) d.doubleValue() : 0;
                if (level == 0) {
                    continue;
                }
                Skill skill = plugin.getSkillRegistry().getOrNull(NamespacedId.fromDefault(k.toLowerCase(Locale.ROOT)));
                if (skill != null) {
                    map.put(skill, level);
                }
            }
        }
        return map;
    }

    private static @Nullable Stat resolveStat(FabricAuraSkills plugin, String rawKey) {
        return plugin.getStatRegistry().getOrNull(parseId(rawKey));
    }

    private static @Nullable Trait resolveTrait(FabricAuraSkills plugin, String rawKey) {
        return plugin.getTraitRegistry().getOrNull(parseId(rawKey));
    }

    private static NamespacedId parseId(String raw) {
        String s = raw;
        if (s.contains(":")) {
            String[] split = s.split(":", 2);
            return NamespacedId.of(split[0].toLowerCase(Locale.ROOT), split[1].toLowerCase(Locale.ROOT));
        }
        return NamespacedId.fromDefault(s.toLowerCase(Locale.ROOT));
    }

    private static String stripNamespace(String rawKey) {
        int i = rawKey.indexOf(':');
        return i >= 0 ? rawKey.substring(i + 1) : rawKey;
    }

    private static @Nullable String firstString(NbtCompound c, String... keys) {
        for (String k : keys) {
            if (c.contains(k) && c.get(k) instanceof NbtString s) {
                return s.value();
            }
        }
        return null;
    }

    private static double firstDouble(NbtCompound c, String... keys) {
        for (String k : keys) {
            if (!c.contains(k)) {
                continue;
            }
            NbtElement el = c.get(k);
            if (el instanceof NbtDouble d) {
                return d.doubleValue();
            }
            if (el instanceof NbtInt i) {
                return i.intValue();
            }
        }
        return 0.0;
    }

    private static String modifierName(Stat stat, ModifierType type, boolean offhand, ItemStack stack) {
        String prefix = StatModifier.ITEM_PREFIX;
        if (type == ModifierType.ITEM) {
            return prefix + "Item." + capitalizeId(stat) + (offhand ? ".Offhand" : "");
        }
        return prefix + "Armor." + armorSlotLabelFromStack(stack) + "." + capitalizeId(stat);
    }

    private static String traitModifierName(Trait trait, ModifierType type, boolean offhand, ItemStack stack) {
        String prefix = TraitModifier.ITEM_PREFIX;
        if (type == ModifierType.ITEM) {
            return prefix + "Item." + capitalizeId(trait) + (offhand ? ".Offhand" : "");
        }
        return prefix + "Armor." + armorSlotLabelFromStack(stack) + "." + capitalizeId(trait);
    }

    private static String capitalizeId(ReloadableIdentifier id) {
        return TextUtil.capitalize(id.name().toLowerCase(Locale.ROOT));
    }

    private static String multiplierName(@Nullable Skill skill, ModifierType type, boolean offhand, ItemStack stack) {
        String skillPart = skill == null ? "Global" : TextUtil.capitalize(skill.toString().toLowerCase(Locale.ROOT));
        if (type == ModifierType.ITEM) {
            return "AuraSkills.Modifiers.Item." + skillPart + (offhand ? ".Offhand" : "");
        }
        return "AuraSkills.Modifiers.Armor." + armorSlotLabelFromStack(stack) + "." + skillPart;
    }

    public static String armorSlotLabelFromStack(ItemStack stack) {
        if (stack.isEmpty()) {
            return "Helmet";
        }
        String path = Registries.ITEM.getId(stack.getItem()).getPath();
        if (path.contains("chestplate") || stack.isOf(Items.ELYTRA)) {
            return "Chestplate";
        }
        if (path.contains("leggings")) {
            return "Leggings";
        }
        if (path.contains("boots")) {
            return "Boots";
        }
        return "Helmet";
    }

    public static ModifierType modifierTypeForEquipmentSlot(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND ? ModifierType.ITEM : ModifierType.ARMOR;
    }
}
