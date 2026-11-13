package dev.granite.graniteskills.bukkit.item;

import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import dev.granite.graniteskills.api.item.ModifierType;
import dev.granite.graniteskills.api.registry.NamespacedId;
import dev.granite.graniteskills.api.stat.Stat;
import dev.granite.graniteskills.api.stat.StatModifier;
import dev.granite.graniteskills.api.util.GraniteSkillsModifier.Operation;
import dev.granite.graniteskills.bukkit.GraniteSkills;
import dev.granite.graniteskills.bukkit.util.ItemUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Modifiers {

    private final GraniteSkills plugin;

    public Modifiers(GraniteSkills plugin) {
        this.plugin = plugin;
    }

    public List<StatModifier> getLegacyModifiers(ModifierType type, ReadWriteNBT nbt) {
        if (plugin.isNbtApiDisabled()) return new ArrayList<>();
        List<StatModifier> modifiers = new ArrayList<>();
        ReadWriteNBT compound = ItemUtils.getLegacyModifiersTypeCompound(nbt, type);
        for (String key : compound.getKeys()) {
            Stat stat = plugin.getStatRegistry().getOrNull(NamespacedId.fromDefault(key.toLowerCase(Locale.ROOT)));
            if (stat != null) {
                double value = compound.getDouble(key);
                modifiers.add(new StatModifier(key, stat, value, Operation.ADD));
            }
        }
        return modifiers;
    }

}
